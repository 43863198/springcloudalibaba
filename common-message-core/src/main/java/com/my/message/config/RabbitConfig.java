package com.my.message.config;

import com.my.message.produce.MessageEventPublisher;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.springframework.amqp.AmqpIOException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.ConditionalExceptionLogger;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnSingleCandidate;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * The type Rabbit config.
 */
@Slf4j
@Configuration
@EnableRabbit
@EnableAsync
@EnableRetry
@EnableConfigurationProperties()
public class RabbitConfig {
    /**
     * Instantiates a new Rabbit config.
     */
    public RabbitConfig() {
    }

    @Value("${password.encode.enabled:false}")
    private boolean encode;
    @Value("${MAX_CONSUMER_COUNT:#{1}}")
    private Long maxConsumerCount;
//    @Autowired
//    private MessageProperties messageProperties;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * Message converter mapping jackson 2 message converter.
     *
     * @return the mapping jackson 2 message converter
     */
    @Bean
    @Primary
    public MappingJackson2MessageConverter messageConverter() {
        return new MappingJackson2MessageConverter();
    }


    @Bean
    public MessageEventPublisher messageEventPublisher(){
        MessageEventPublisher messageEventPublisher = new MessageEventPublisher();
        messageEventPublisher.setApplicationEventPublisher(applicationEventPublisher);
        return messageEventPublisher;
    }

    /**
     * Json message converter jackson 2 json message converter.
     *
     * @return the jackson 2 json message converter
     */
    @Bean
    @Primary
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Rabbit connection factory caching connection factory.
     *
     * @param properties             the properties
     * @param connectionNameStrategy the connection name strategy
     * @param encode                 the encode
     * @return the caching connection factory
     * @throws Exception the exception
     */
    @Bean
    @Primary
    public CachingConnectionFactory rabbitConnectionFactory(RabbitProperties properties, ObjectProvider<ConnectionNameStrategy> connectionNameStrategy, @Value("${password.encode.enabled:false}") boolean encode) throws Exception {
        PropertyMapper map = PropertyMapper.get();
        CachingConnectionFactory factory = new CachingConnectionFactory(
                getRabbitConnectionFactoryBean(properties).getObject());
        map.from(properties::determineAddresses).to(factory::setAddresses);
        //map.from(properties::isPublisherConfirms).to(factory::setPublisherConfirms);
        map.from(properties::isPublisherReturns).to(factory::setPublisherReturns);
        RabbitProperties.Cache.Channel channel = properties.getCache().getChannel();
        map.from(channel::getSize).whenNonNull().to(factory::setChannelCacheSize);
        map.from(channel::getCheckoutTimeout).whenNonNull().as(Duration::toMillis)
                .to(factory::setChannelCheckoutTimeout);
        RabbitProperties.Cache.Connection connection = properties.getCache().getConnection();
        map.from(connection::getMode).whenNonNull().to(factory::setCacheMode);
        map.from(connection::getSize).whenNonNull().to(factory::setConnectionCacheSize);
        map.from(connectionNameStrategy::getIfUnique).whenNonNull().to(factory::setConnectionNameStrategy);
        factory.setCloseExceptionLogger(new OcmsDefaultChannelCloseLogger());
        return factory;
    }

    private RabbitConnectionFactoryBean getRabbitConnectionFactoryBean(RabbitProperties properties) {
        PropertyMapper map = PropertyMapper.get();
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        map.from(properties::determineHost).whenNonNull().to(factory::setHost);
        map.from(properties::determinePort).to(factory::setPort);
        map.from(properties::determineUsername).whenNonNull().to(factory::setUsername);
        //map.from(properties::determinePassword).whenNonNull().to(factory::setPassword);
        map.from(properties::determinePassword).whenNonNull().to((s) -> {
            if (encode) {
                s = new String(Base64.decodeBase64(s), StandardCharsets.UTF_8);
            }

            factory.setPassword(s);
        });
        map.from(properties::determineVirtualHost).whenNonNull().to(factory::setVirtualHost);
        map.from(properties::getRequestedHeartbeat).whenNonNull().asInt(Duration::getSeconds)
                .to(factory::setRequestedHeartbeat);
        RabbitProperties.Ssl ssl = properties.getSsl();
        if (ssl.determineEnabled()) {
            factory.setUseSSL(true);
            map.from(ssl::getAlgorithm).whenNonNull().to(factory::setSslAlgorithm);
            map.from(ssl::getKeyStoreType).to(factory::setKeyStoreType);
            map.from(ssl::getKeyStore).to(factory::setKeyStore);
            map.from(ssl::getKeyStorePassword).to(factory::setKeyStorePassphrase);
            map.from(ssl::getTrustStoreType).to(factory::setTrustStoreType);
            map.from(ssl::getTrustStore).to(factory::setTrustStore);
            map.from(ssl::getTrustStorePassword).to(factory::setTrustStorePassphrase);
            map.from(ssl::isValidateServerCertificate)
                    .to((validate) -> factory.setSkipServerCertificateValidation(!validate));
            map.from(ssl::getVerifyHostname).to(factory::setEnableHostnameVerification);
        }
        map.from(properties::getConnectionTimeout).whenNonNull().asInt(Duration::toMillis)
                .to(factory::setConnectionTimeout);
        // we don't use RPC calls in OCMS, this's only for creating channel
        factory.setChannelRpcTimeout((int) SECONDS.toMillis(5));
        factory.afterPropertiesSet();
        return factory;
    }

    /**
     * The type Rabbit template configuration.
     */
    @Configuration
    protected static class RabbitTemplateConfiguration {

        private final RabbitProperties properties;

        private final ObjectProvider<MessageConverter> messageConverter;

//        @Autowired
//        private MessageProperties messageProperties;

        private final ObjectProvider<RabbitRetryTemplateCustomizer> retryTemplateCustomizers;

        /**
         * Instantiates a new Rabbit template configuration.
         *
         * @param properties               the properties
         * @param messageConverter         the message converter
         * @param retryTemplateCustomizers the retry template customizers
         */
        public RabbitTemplateConfiguration(RabbitProperties properties,
                                           ObjectProvider<MessageConverter> messageConverter,
                                           ObjectProvider<RabbitRetryTemplateCustomizer> retryTemplateCustomizers) {
            this.properties = properties;
            this.messageConverter = messageConverter;
            this.retryTemplateCustomizers = retryTemplateCustomizers;
        }

        /**
         * Rabbit template rabbit template.
         *
         * @param connectionFactory the connection factory
         * @return the rabbit template
         */
        @Bean
        @Primary
        @ConditionalOnSingleCandidate(ConnectionFactory.class)
        public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
            PropertyMapper map = PropertyMapper.get();
            RabbitTemplate template = new RabbitTemplate(connectionFactory);
            MessageConverter messageConverter = this.messageConverter.getIfUnique();
            if (messageConverter != null) {
                template.setMessageConverter(messageConverter);
            }
            template.setMandatory(determineMandatoryFlag());
            RabbitProperties.Template properties = this.properties.getTemplate();
            if (properties.getRetry().isEnabled()) {
                template.setRetryTemplate(new RetryTemplateFactory(
                        this.retryTemplateCustomizers.orderedStream().collect(Collectors.toList())).createRetryTemplate(
                        properties.getRetry(), RabbitRetryTemplateCustomizer.Target.SENDER));
            }
            map.from(properties::getReceiveTimeout).whenNonNull().as(Duration::toMillis)
                    .to(template::setReceiveTimeout);
            map.from(properties::getReplyTimeout).whenNonNull().as(Duration::toMillis).to(template::setReplyTimeout);
            map.from(properties::getExchange).to(template::setExchange);
            map.from(properties::getRoutingKey).to(template::setRoutingKey);
            map.from(properties::getDefaultReceiveQueue).whenNonNull().to(template::setDefaultReceiveQueue);
            template.setUsePublisherConnection(true);
            template.setUserCorrelationId(true);
            return template;
        }

        private boolean determineMandatoryFlag() {
            Boolean mandatory = this.properties.getTemplate().getMandatory();
            return (mandatory != null) ? mandatory : this.properties.isPublisherReturns();
        }

        /**
         * Amqp admin amqp admin.
         *
         * @param connectionFactory the connection factory
         * @return the amqp admin
         */
        @Bean
        @Primary
        @ConditionalOnSingleCandidate(ConnectionFactory.class)
        @ConditionalOnProperty(prefix = "spring.rabbitmq", name = "dynamic", matchIfMissing = true)
        public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
            return new RabbitAdmin(connectionFactory);
        }
    }

    private static class OcmsDefaultChannelCloseLogger implements ConditionalExceptionLogger {

        /**
         * Instantiates a new Ocms default channel close logger.
         */
        OcmsDefaultChannelCloseLogger() {
            super();
        }

        @Override
        public void log(Log logger, String message, Throwable t) {
            if (t instanceof ShutdownSignalException) {
                ShutdownSignalException cause = (ShutdownSignalException) t;

                // Normal channel closes (200 OK)
                if(RabbitUtils.isNormalChannelClose(cause)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(message + ": " + cause.getMessage());
                    }

                    // channel is closed due to a failed passive queue declaration
                } else if (RabbitUtils.isPassiveDeclarationChannelClose(cause)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(message + ": " + cause.getMessage());
                    }

                    // channel is closed because the basic.consume is refused due to an exclusive consumer condition
                } else if (RabbitUtils.isExclusiveUseChannelClose(cause)) {
                    if (logger.isInfoEnabled()) {
                        logger.info(message + ": " + cause.getMessage());
                    }
                    //
                } else if (!RabbitUtils.isNormalChannelClose(cause)) {
                    logger.error(message + ": " + cause.getMessage());
                }
            } else {
                logger.error("Unexpected invocation of " + this.getClass() + ", with message: " + message, t);
            }
        }
    }


    /**
     * Custom partition selector custom partition selector class.
     *
     * @return the custom partition selector class
     */
    @Bean
    public CustomPartitionSelectorClass customPartitionSelector() {
        return new CustomPartitionSelectorClass();
    }

    /**
     * Msg task executor task executor.
     *
     * @return the task executor
     */
    @Bean
    public TaskExecutor msgTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(64);
        executor.setKeepAliveSeconds(60);
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("[MQ] Task {} discarded from {}.", r.toString(), e.toString());
        });
        executor.setThreadNamePrefix("rabbit-msg-");

        return executor;
    }

    /**
     * Optimistic lock retry template retry template.
     *
     * @return the retry template
     */
    @Bean("optimisticLockRetryTemplate")
    public RetryTemplate optimisticLockRetryTemplate() {
        // retry policy
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
//        retryableExceptions.put(ObjectOptimisticLockingFailureException.class, true);
//        retryableExceptions.put(StaleObjectStateException.class, true);
//        retryableExceptions.put(OptimisticEntityLockException.class, true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(Math.toIntExact(maxConsumerCount), retryableExceptions);

        // backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(400L);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(2000L);

        // retry template
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        template.setThrowLastExceptionOnExhausted(true);

        return template;
    }

    /**
     * Dead letter retry template retry template.
     *
     * @return the retry template
     */
    @Bean("deadLetterRetryTemplate")
    public RetryTemplate deadLetterRetryTemplate() {
        // retry policy
        Map<Class<? extends Throwable>, Boolean> retryableExceptions = new HashMap<>();
        retryableExceptions.put(AmqpIOException.class, true);
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(Math.toIntExact(maxConsumerCount), retryableExceptions);

        // backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(400L);
        backOffPolicy.setMultiplier(2);
        backOffPolicy.setMaxInterval(2000L);

        // retry template
        RetryTemplate template = new RetryTemplate();
        template.setRetryPolicy(retryPolicy);
        template.setBackOffPolicy(backOffPolicy);
        template.setThrowLastExceptionOnExhausted(true);

        return template;
    }


    @Bean(name = "eventMap")
    public ConcurrentHashMap<String,Object> eventMap(){
        return new ConcurrentHashMap<>(64);
    }
}
