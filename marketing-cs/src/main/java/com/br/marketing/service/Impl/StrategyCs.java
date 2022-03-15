package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.DtbStrategyClient;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by Bairong on 2020/1/19.
 */
@Service
@Slf4j
public class StrategyCs {
    @Resource
    DtbStrategyClient dtbStrategyClient;

    public String strategyIdCheck(String apiCode, String strategyId) {
        String result="";
        if(!strategyId.startsWith("DTM")){
            return result;
        }
        String str = dtbStrategyClient.getStrategy(apiCode, strategyId);
        if (StringUtils.isEmpty(str)) {
            log.error("数据策略查询结果为空-{}-{}",apiCode,strategyId);
            return "";
        } else {
            JSONObject strategy = JSONObject.parseObject(str);
            if ("000000".equals(strategy.getString("code"))) {
                JSONObject data = strategy.getJSONObject("data");
                if (data != null && StringUtils.isNotEmpty(data.getString("dataProdList"))
                        && data.getInteger("status") == 1 && data.getInteger("canUse") == 1) {
                    JSONObject dtbStrategy = strategy.getJSONObject("data");
                     result= JSON.parseObject(dtbStrategy.getString("dataProdList")).getString("dataProdList");
                     if(StringUtils.isEmpty(result)){
                         log.error("数据策略配置为空--{}",strategy);
                         return "";
                     }
                    JSONArray jsonArray=JSONArray.parseArray(result);
                    log.info("dtbStrategyArray---{}", jsonArray);
                    if (jsonArray.size() == 0) {
                        log.error("数据策略配置为空--{}",strategy);
                        return "";
                    }
                } else {
                    log.error("数据策略不可用--{}",strategy);
                    return "";
                }
            } else {
                log.error("数据策略查询异常--{}",strategy);
                return "";
            }
        }
        return result;
    }
}
