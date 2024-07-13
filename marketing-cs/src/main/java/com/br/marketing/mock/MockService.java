package com.br.marketing.mock;

import com.alibaba.fastjson.JSON;
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
        JSONObject mockConfig = getMockConfig(interfaceCode);
        HashMap<String, String> mockContent = convertJsonToHashMap(mockConfig.getString("mockContent"));
        return mockContent;
    }

    public JSONObject getMockConfig(String interfaceCode){
        Map<String, JSONObject> commonMockConfig = marketingCommonConfig.getCommonMockConfig();
        JSONObject mockConfig = commonMockConfig.get(interfaceCode);
        return mockConfig;
    }

    public static HashMap<String, String> convertJsonToHashMap(String jsonString) {
        JSONObject jsonObject = JSON.parseObject(jsonString);

        HashMap<String, String> map = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            map.put(key, jsonObject.getString(key));
        }

        return map;
    }
}