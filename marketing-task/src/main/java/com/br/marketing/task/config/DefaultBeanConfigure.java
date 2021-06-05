/*
package com.br.marketing.task.bean;

import org.apache.http.impl.config.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.config.HttpComponentsClientHttpRequestFactory;
import org.springframework.controller.config.RestTemplate;


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
