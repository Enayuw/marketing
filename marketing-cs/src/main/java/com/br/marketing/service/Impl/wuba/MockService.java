package com.br.marketing.service.Impl.wuba;

import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;

/**
 * @Description WuBaDingDingService
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class MockService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    public boolean checkMockSwitch(String interfaceCode){

        return false;
    }

    private HashMap<String, String> getMock() {
        return new HashMap<>();
    }
}
