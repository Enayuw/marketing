package com.br.marketing.innerapi.config;


import org.apache.http.HttpResponse;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
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

}
