package com.br.marketing.innerapi.controller.auth;

import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 策略
 * @Date 2022/3/23 1:51 PM
 * ------------------------------
 */
@RestController
@Api(value = "策略调用", tags = "strategy")
@RequestMapping("strategy-customizer")
public class CustomerStrategyController {
    @Resource
    RestTemplate restTemplate;
    private static String STRATEGY_DISTRIBUTION_LIST = "http://STRATEGY-DISTRIBUTION/strategy-customizer/distributeList?" +
            "apiCode={apiCode}&strategyCategory={strategyCategory}&distributeType={distributeType}&strategyType={strategyType}";
    private static String CREATE_VIEW = "http://STRATEGY-DISTRIBUTION/strategy-customizer/createView?" +
            "apiCode={apiCode}&strategyCategory={strategyCategory}&distributeType={distributeType}&strategyType={strategyType}";
    @GetMapping("distributeList")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public JSONObject distributeList(String apiCode,String strategyCategory,String distributeType,String strategyType) {
        Map<String, Object> urlVariables = getStringObjectMap(apiCode, strategyCategory, distributeType, strategyType);
        String result = restTemplate.getForObject(STRATEGY_DISTRIBUTION_LIST, String.class, urlVariables);
        return (JSONObject) JSONObject.parse(result);
    }
    @GetMapping("createView")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public JSONObject createView(String apiCode,String strategyCategory,String distributeType,String strategyType) {
        Map<String, Object> urlVariables = getStringObjectMap(apiCode, strategyCategory, distributeType, strategyType);
        String result = restTemplate.getForObject(CREATE_VIEW, String.class, urlVariables);
        return (JSONObject) JSONObject.parse(result);
    }
    private Map<String, Object> getStringObjectMap(String apiCode, String strategyCategory, String distributeType, String strategyType) {
        Map<String, Object> urlVariables = new HashMap<>();
        urlVariables.put("apiCode", apiCode);
        urlVariables.put("strategyCategory", strategyCategory);
        urlVariables.put("distributeType", distributeType);
        urlVariables.put("strategyType", strategyType);
        return urlVariables;
    }


}
