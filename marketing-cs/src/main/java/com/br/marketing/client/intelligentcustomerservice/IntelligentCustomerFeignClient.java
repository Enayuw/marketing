package com.br.marketing.client.intelligentcustomerservice;

import feign.hystrix.FallbackFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;


@FeignClient(value = "b-house-service",fallbackFactory = IntelligentCustomerFeignClient.FallIntelligentCustomerFeignClient.class)
public interface IntelligentCustomerFeignClient {

    @Component
    class FallIntelligentCustomerFeignClient implements FallbackFactory<IntelligentCustomerFeignClient> {

        @Override
        public IntelligentCustomerFeignClient create(Throwable throwable) {
            return new IntelligentCustomerFeignClient(){
            };
        }
    }
}
