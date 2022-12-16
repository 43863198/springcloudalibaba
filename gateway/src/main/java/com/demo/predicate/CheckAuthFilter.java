package com.demo.predicate;

import io.netty.util.internal.StringUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 全局过滤器，也有局部过滤器实现gatewayFillter
 * WebFIlter是属于SpringBoot体现的，适用于Spring Web请求。
 * GlobalFilter和GatewayFIlter属于SpringCloud体系的，适用于Spring Gateway中使用
 *
 * -Dreactor.netty.http.server.accessLogEnabled=true 开启日志
 */
@Component
public class CheckAuthFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("全局过滤器执行");
//        List<String> token = exchange.getRequest().getHeaders().get("token");
//        if(null == token){
//            return Mono.empty();
//        }
        return chain.filter(exchange);
    }
}
