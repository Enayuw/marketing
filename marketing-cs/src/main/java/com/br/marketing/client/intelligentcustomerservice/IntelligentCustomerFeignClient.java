package com.br.marketing.client.intelligentcustomerservice;

import com.br.marketing.client.intelligentcustomerservice.output.TransferRobotOutboundVO;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "ROBOTAI-API-SERVICE",fallbackFactory = IntelligentCustomerFeignClient.FallIntelligentCustomerFeignClient.class)
public interface IntelligentCustomerFeignClient {

    private static final Logger log = LoggerFactory.getLogger(IntelligentCustomerFeignClient.class);

    @GetMapping(value = "/ /api/robotOutbound")
    TransferRobotOutboundVO robotOutbound(@RequestParam("apiCode") String apiCode,@RequestParam("jsonData") String jsonData);

    @Component
    class FallIntelligentCustomerFeignClient implements FallbackFactory<IntelligentCustomerFeignClient> {

        @Override
        public IntelligentCustomerFeignClient create(Throwable throwable) {
            return new IntelligentCustomerFeignClient(){
                @Override
                public TransferRobotOutboundVO robotOutbound(String apiCode,String jsonData) {
                    if(throwable!=null){
                        log.error(throwable.getMessage(),throwable);
                        return new TransferRobotOutboundVO
                    }
                }
            };
        }
    }
}
