package com.br.marketing.client.intelligentcustomerservice;

import com.br.marketing.client.FeignFormConfiguration;
import com.br.marketing.client.intelligentcustomerservice.output.TransferRobotOutboundVO;
import feign.hystrix.FallbackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@FeignClient(name = "robotAI-api-service", configuration = FeignFormConfiguration.class,fallbackFactory = IntelligentCustomerFeignClient.FallIntelligentCustomerFeignClient.class)
public interface IntelligentCustomerFeignClient {

    static final Logger log = LoggerFactory.getLogger(IntelligentCustomerFeignClient.class);

    @PostMapping(value = "/api/robotOutbound",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TransferRobotOutboundVO robotOutbound(String body);

    @Component
    class FallIntelligentCustomerFeignClient implements FallbackFactory<IntelligentCustomerFeignClient> {

        @Override
        public IntelligentCustomerFeignClient create(Throwable throwable) {
            return new IntelligentCustomerFeignClient(){
                @Override
                public TransferRobotOutboundVO robotOutbound(String body) {
                    log.error(throwable.getMessage(),throwable);
                    TransferRobotOutboundVO transferRobotOutboundVO = new TransferRobotOutboundVO();
                    transferRobotOutboundVO.setCode("9999");
                    transferRobotOutboundVO.setMessage(throwable.getMessage());
                    return transferRobotOutboundVO;
                }
            };
        }
    }
}
