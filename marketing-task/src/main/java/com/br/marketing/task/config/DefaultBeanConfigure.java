/*
package com.br.marketing.task.config;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


*/
/** 默认bean配置器
 * @author
 * @since 2018/3/15
 *//*

@Configuration
public class DefaultBeanConfigure {

    @Bean
    RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory(
                HttpClientBuilder.create().setMaxConnPerRoute(500).setMaxConnTotal(1000).build());
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(1000);
        httpRequestFactory.setReadTimeout(5000);
        RestTemplate restTemplate =new RestTemplate(httpRequestFactory);
        return restTemplate;
    }


}
*/
