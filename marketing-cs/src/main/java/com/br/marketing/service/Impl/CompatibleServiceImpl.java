package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.ICompatibleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.ConfigByApiCodeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class CompatibleServiceImpl implements ICompatibleService {


    @Value("${cluster.flag}")
    private String clusterConfig;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Override
    public Boolean isAction(String config) {
        HashMap<String, String> moveConfig = marketingCommonConfig.getMoveConfig();
        if(moveConfig != null && !"1".equals(moveConfig.get("jobToEngineRoom"))){
            return true;
        }

        if ("k8s-prod-c".equals(clusterConfig) || "k8s-prod-d".equals(clusterConfig)) {
            if (StringUtils.isBlank(config)) {
                return false;
            }
            ConfigByApiCodeVO o = JSON.parseObject(config, new TypeReference<ConfigByApiCodeVO>() {
            }.getType());

            if ("yz".equals(o.getActionEnv())) {
                return true;
            } else {
                return false;
            }
        } else {
            if (StringUtils.isBlank(config)) {
                return true;
            }
            ConfigByApiCodeVO o = JSON.parseObject(config, new TypeReference<ConfigByApiCodeVO>() {
            }.getType());

            if ("yz".equals(o.getActionEnv())) {
                return false;
            } else {
                return true;
            }
        }
    }
}
