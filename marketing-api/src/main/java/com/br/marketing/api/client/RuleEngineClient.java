package com.br.marketing.api.client;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.client.RuleResult;
import com.br.marketing.common.utils.transaction.SwiftNumberManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

/** 规则引擎访问客户端，通过ribbon调用规则引擎
 * @author Wang Weiwei
 * @since 2018/3/19
 */
@Component
@Slf4j
public class RuleEngineClient {
    @Resource
    private RestTemplate restTemplate;


    /**
     * Query 1 rule result.
     *
     * @param param the param
     * @return the rule result
     */
    public RuleResult query1(JSONObject param) {
        param.put("code", "03");
        param.put("swift_number", SwiftNumberManager.generate());
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("params", param.toJSONString());
        //log.info("请求规则引擎参数--{}", JSON.toJSONString(param));
        String re = restTemplate.postForObject("http://rule-service-simple/ruleEngine/query1", form, String.class);
        return new RuleResult(re);
    }
}