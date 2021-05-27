package com.br.marketing.api.config;

import com.br.marketing.common.utils.net.ApiCaller;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AppConfig {



//    @Bean
//    @Scope(proxyMode= ScopedProxyMode.TARGET_CLASS,value = "prototype")
//    public ApiCaller apiCaller(){
//      return  new ApiCaller(new RestTemplate());
//    }
}
