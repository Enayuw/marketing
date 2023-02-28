package com.br.marketing.tools.config;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Configuration
public class DefaultBean {
    @Primary
    @Bean
        //根据环境变量RPC_MODE（在marmot deployment.yaml配置）来决定是否去除ribbon负载均衡功能，配置值为ISTIO_ETCD时，加载该bean，禁用ribbon
//    @ConditionalOnExpression("#{'ISTIO_ETCD'.equals('${rpc.mode}')}")
    RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().setMaxConnPerRoute(500).setMaxConnTotal(1000)
                                                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                            @Override
                            public long getKeepAliveDuration(final HttpResponse response, final HttpContext context) {
                                long keepAlive = super.getKeepAliveDuration(response, context);
                                if (keepAlive == -1) {
                                    keepAlive = 5000;
                                }
//                                return keepAlive;
                                  return -1;
                            }
                        })
                        .evictIdleConnections(10, TimeUnit.SECONDS)
                        .build());
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(1000);
        httpRequestFactory.setReadTimeout(10000);
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
//        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .set(1, new StringHttpMessageConverter());
        return restTemplate;
    }
}
