package com.br.marketing.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProFieldsClient {
    @Value("${otherConfig.warning.ruleList:00}")
    private String rules;


    /**
     * 设置贷中产品
     * @param strategyStr 策略字符串
     * @param meal
     */
    public void setLoanPro(String strategyStr,JSONObject meal){
            JSONArray dtbArray=new JSONArray();
            if(StringUtils.isNotBlank(strategyStr)){
                try {
                    dtbArray=JSONArray.parseArray(strategyStr);
                }catch (Exception e){
                    log.error("strategyStr格式化错误",e);
                }
                for(int i=0;i<dtbArray.size();i++){
                    JSONObject jsonObject = dtbArray.getJSONObject(i);
                    JSONObject jsonObject1=new JSONObject();
                    jsonObject1.put(jsonObject.getString("code"),jsonObject.getString("version"));
                    JSONObject versionJson=new JSONObject();
                    versionJson.put("version",jsonObject.getString("version"));
                    meal.put(jsonObject.getString("code"),versionJson);
                }
            }
    }
}

