package com.br.marketing.innerapi.controller.auth;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

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
    private static final String STRATEGY_DISTRIBUTION_LIST = "http://STRATEGY-DISTRIBUTION/strategy-customizer/distributeList?" +
            "apiCode={apiCode}&strategyCategory={strategyCategory}&distributeType={distributeType}&strategyType={strategyType}";

    @Value("${api.productManagement.url}")
    private  String productUrl;
    @GetMapping("distributeList")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public JSONObject distributeList(String apiCode,String strategyCategory,String distributeType,String strategyType) {
        Map<String, Object> urlVariables = getStringObjectMap(apiCode, strategyCategory, distributeType, strategyType);
        String result = restTemplate.getForObject(STRATEGY_DISTRIBUTION_LIST, String.class, urlVariables);
        return (JSONObject) JSONObject.parse(result);
    }
    //productChineseName、productName、secondTypeName、spreadStatus、version、versions
    @GetMapping("createView")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<List<Map<String, Object>>> createView() {
        String result = restTemplate.getForObject(productUrl, String.class);
        JSONObject jsonObject = (JSONObject) JSONObject.parse(result);
        JSONArray datArray = jsonObject.getJSONArray("data");
        List<Map<String, Object>> dataList = new ArrayList<>();
        Set<String> productSet = new TreeSet<>();

        for(int i=0;i<datArray.size();i++){
            Map<String, Object> productMap = new HashMap<>();
            String productionChineseName = datArray.getJSONObject(i).getString("productionChineseName");
            String productName = datArray.getJSONObject(i).getString("productionName");
            String secondTypeName = datArray.getJSONObject(i).getString("productionTypeCode");
            String spreadStatus = datArray.getJSONObject(i).getString("spreadStatus");
            String version = datArray.getJSONObject(i).getString("version");
            productMap.put("productionChineseName",productionChineseName);
            productMap.put("productName",productName);
            productMap.put("secondTypeName",secondTypeName);
            productMap.put("spreadStatus",spreadStatus);
            productMap.put("versions",version);
            dataList.add(productMap);
            productSet.add(productName);
        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        for(String productSetName : productSet){
            List<String> versionList = new ArrayList<>();
            Map<String, Object> productMap = new HashMap<>();
            for (Map<String, Object> stringObjectMap : dataList) {
                Object productName = stringObjectMap.get("productName");
                String version = stringObjectMap.get("versions").toString();
                if (productName.equals(productSetName)) {
                    versionList.add(version);
                    productMap = stringObjectMap;
                }
            }
            productMap.put("versions",versionList);
            resultList.add(productMap);
        }
        return new ApiResult<List<Map<String, Object>>>().success(resultList);
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
