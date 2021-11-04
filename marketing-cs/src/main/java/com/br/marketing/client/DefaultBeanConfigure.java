package com.br.marketing.client;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;


/**
 * 默认bean配置器
 *
 * @author
 * @since 2018/3/15
 */
@Configuration
public class DefaultBeanConfigure {

    @Value("${otherConfig.proxy.proxy_host_zw:00}")
    private  String  proxyHost;
    @Value("${otherConfig.proxy.proxy_port:00}")
    private  int proxyPort;
    @Value("${otherConfig.proxy.proxy_username:00}")
    private  String userName;
    @Value("${otherConfig.proxy.proxy_password:00}")
    private  String password;

    @Bean
    @LoadBalanced
    //根据环境变量RPC_MODE（在marmot deployment.yaml配置）来决定是否增加ribbon负载均衡功能，不配置或配置值为RIBBON_EUREKA时，加载该bean
    @ConditionalOnExpression("#{'RIBBON_EUREKA'.equals('${rpc.mode:RIBBON_EUREKA}')}")
    RestTemplate loadBalanced() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(1000);
        httpRequestFactory.setReadTimeout(5000);
        return new RestTemplate(httpRequestFactory);
    }

    @Primary
    @Bean
    //根据环境变量RPC_MODE（在marmot deployment.yaml配置）来决定是否去除ribbon负载均衡功能，配置值为ISTIO_ETCD时，加载该bean，禁用ribbon
//    @ConditionalOnExpression("#{'ISTIO_ETCD'.equals('${rpc.mode}')}")
    RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().setMaxConnPerRoute(500).setMaxConnTotal(1000).build());
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(1000);
        httpRequestFactory.setReadTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        restTemplate.getMessageConverters()
                .set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }

    @Bean(name = "restTemplateByProxy")
    RestTemplate restTemplateByProxy(){
        // 设置代理HttpHost
        HttpHost proxy = new HttpHost(proxyHost, proxyPort );
        // 设置认证
        CredentialsProvider provider = new BasicCredentialsProvider();

        provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(userName, password));
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().setMaxConnPerRoute(500).setMaxConnTotal(1000)
                        .setProxy(proxy).setDefaultCredentialsProvider(provider).build());
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(1000);
        httpRequestFactory.setReadTimeout(5000);
        RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
        restTemplate.getMessageConverters()
                .set(1, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        return restTemplate;
    }
}
