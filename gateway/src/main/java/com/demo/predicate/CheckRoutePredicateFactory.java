package com.demo.predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.cloud.gateway.handler.predicate.GatewayPredicate;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@Component
public class CheckRoutePredicateFactory extends AbstractRoutePredicateFactory<CheckRoutePredicateFactory.Config> {
    /**
     * Param key.
     */
    public static final String PARAM_KEY = "param";

    /**
     * Regexp key.
     */
    public static final String REGEXP_KEY = "regexp";

    public CheckRoutePredicateFactory() {
        super(CheckRoutePredicateFactory.Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("name");
    }

    @Override
    public Predicate<ServerWebExchange> apply(CheckRoutePredicateFactory.Config config) {
        return new GatewayPredicate(){

            @Override
            public boolean test(ServerWebExchange serverWebExchange) {
                if(config.getName().equals("jimmy")){
                    return true;
                }
                return false;
            }
        };
    }

    @Validated
    public static class Config {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
