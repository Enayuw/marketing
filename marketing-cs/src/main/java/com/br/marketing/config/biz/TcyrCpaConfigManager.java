package com.br.marketing.config.biz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.entity.TcyrCpaFailMsgConfig;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.tccpa.TcyrCpaFailMsgVO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TcyrCpaConfigManager {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 获取原始配置
     */
    private List<TcyrCpaFailMsgConfig> createFailMsgConfig() {
        List<JSONObject> tcyrCpaFailMsgConfig = marketingCommonConfig.getTcyrCpaFailMsgConfig();
        try {
            String jsonString = JSON.toJSONString(tcyrCpaFailMsgConfig);
            List<TcyrCpaFailMsgConfig> configs = JSON.parseObject(
                    jsonString,
                    new TypeReference<List<TcyrCpaFailMsgConfig>>() {}
            );
            return configs;
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }

    /**
     * 获取failMsg-lockBelong的映射Map
     */
    public Map<Integer, Integer> getFailMsgToBlMap() {
        return createFailMsgConfig().stream()
                .filter(TcyrCpaFailMsgConfig::isIfWithReleaseTime)
                .filter(c -> c.getValue() != null)
                .filter(c -> c.getLockBelong() != null)
                .collect(Collectors.toMap(
                        TcyrCpaFailMsgConfig::getValue,
                        TcyrCpaFailMsgConfig::getLockBelong,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * 获取failMsg-lockBelong的映射Map
     * String版
     */
    public Map<String, Integer> getFailMsgToBlMapVT() {
        return createFailMsgConfig().stream()
                .filter(TcyrCpaFailMsgConfig::isIfWithReleaseTime)
                .filter(c -> c.getValue() != null)
                .filter(c -> c.getLockBelong() != null)
                .collect(Collectors.toMap(
                        c -> String.valueOf(c.getValue()),
                        TcyrCpaFailMsgConfig::getLockBelong,
                        (existing, replacement) -> existing

                ));
    }

    /**
     * 获取lockBelong-failMsg的映射Map
     */
    public Map<Integer, Integer> getBelongToFmMap() {
        return createFailMsgConfig().stream()
                .filter(TcyrCpaFailMsgConfig::isIfWithReleaseTime)
                .filter(c -> c.getValue() != null)
                .filter(c -> c.getLockBelong() != null)
                .collect(Collectors.toMap(
                        TcyrCpaFailMsgConfig::getLockBelong,
                        TcyrCpaFailMsgConfig::getValue,
                        (existing, replacement) -> existing
                ));
    }

    /**
     * 获取failMsg的VO
     */
    public List<TcyrCpaFailMsgVO> getFailMsgVOs() {
        try {
            String jsonString = JSON.toJSONString(createFailMsgConfig());
            List<TcyrCpaFailMsgVO> vos = JSON.parseObject(
                    jsonString,
                    new TypeReference<List<TcyrCpaFailMsgVO>>() {}
            );
            return vos;
        } catch (Exception e) {
            throw new RuntimeException("JSON转换失败", e);
        }
    }

}
