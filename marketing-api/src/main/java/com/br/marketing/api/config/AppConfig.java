package com.br.marketing.api.config;

import com.br.marketing.api.aspect.ErrorControllerAspect;
import com.br.marketing.common.utils.net.ApiCaller;
import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 托给容器管理的对象
 */
@Component
public class AppConfig {


    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

//    @Bean
//    @Scope(proxyMode= ScopedProxyMode.TARGET_CLASS,value = "prototype")
//    public ApiCaller apiCaller(){
//      return  new ApiCaller(new RestTemplate());
//    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {

        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(600);
        manager.setDefaultMaxPerRoute(400);
        manager.setValidateAfterInactivity(5 * 1000);

        final CloseableHttpClient client = HttpClients.custom()
                .setConnectionManager(manager)
                .setConnectionManagerShared(false)
                .setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {
                    @Override
                    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                        long keepAliveDuration = DefaultConnectionKeepAliveStrategy.INSTANCE.getKeepAliveDuration(response, context);
                        if(keepAliveDuration <= 0){
                            keepAliveDuration = 10*1000;
                        }

                        return keepAliveDuration;
                    }
                })
                .evictIdleConnections(10, TimeUnit.SECONDS)
                .evictExpiredConnections()
                .build();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    client.close();
                } catch (Exception e) {
                    if(log.isErrorEnabled()){
                        log.error(e.getMessage(),e);
                    }
                }
            }
        });

        clientHttpRequestFactory.setHttpClient(client);

        clientHttpRequestFactory.setConnectTimeout(3000);
        clientHttpRequestFactory.setReadTimeout(10000);

        clientHttpRequestFactory.setConnectionRequestTimeout(2000);

        return clientHttpRequestFactory;
    }

    /**
     * RestTemplate的单例
     * @return
     */
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
        restTemplate.setRequestFactory(getClientHttpRequestFactory());
        return restTemplate;
    }
}
