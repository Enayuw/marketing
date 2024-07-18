package com.br.marketing.mock;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description MockService
 * @Author lixiang
 * @Date 2024-07-10
 */
@Service
@Slf4j
public class MockService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    public boolean checkMockSwitch(String interfaceCode){
        JSONObject mockConfig = getMockConfig(interfaceCode);
        String mockSwitch = mockConfig.getString("mockSwitch");
        return "1".equals(mockSwitch);
    }

    public HashMap<String, String> getMockContent(String interfaceCode) {
        HashMap<String, String> res = new HashMap<>();
        JSONObject mockConfig = getMockConfig(interfaceCode);
        JSONObject mockContent = mockConfig.getJSONObject("mockContent");
        for(Map.Entry<String, Object> entry : mockContent.entrySet()){
            res.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return res;
    }

    public JSONObject getMockConfig(String interfaceCode){
        JSONObject commonMockConfig = marketingCommonConfig.getCommonMockConfig();
        JSONObject mockConfig = commonMockConfig.getJSONObject(interfaceCode);
        return mockConfig;
    }
}
