package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.enums.ClusterEnum;
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
    public Boolean isAction(String config,String jobName) {
        HashMap<String, String> moveConfig = marketingCommonConfig.getMoveConfig();
        if(moveConfig != null && moveConfig.get("jobToEngineRoom") !=null && !"1".equals(moveConfig.get("jobToEngineRoom"))){
            return true;
        }
        if(moveConfig != null && moveConfig.get(jobName) !=null && !"1".equals(moveConfig.get(jobName))){
            return true;
        }

        if (ClusterEnum.CLUSTER_PROD_C.getName().equals(clusterConfig) || ClusterEnum.CLUSTER_PROD_D.getName().equals(clusterConfig)) {
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
