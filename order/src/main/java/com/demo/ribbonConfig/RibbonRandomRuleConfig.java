//package com.demo.ribbonConfig;
//
//import com.netflix.loadbalancer.BestAvailableRule;
//import com.netflix.loadbalancer.IRule;
//import com.netflix.loadbalancer.RandomRule;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
///**
// * 1 > 基于代码实现自定义得ribbon策略，需要配合启动类上得ribbonClients 指定不同得服务使用不同得规则
// * 2 > 基于配置文件得
// */
//@Configuration
//public class RibbonRandomRuleConfig {
//
//    /**
//     * 可以实现定义好的得rule
//     * @return
//     */
//    @Bean
//    public IRule iRule(){
//        //return new RandomRule();
//        return new BestAvailableRule();
//    }
//}
