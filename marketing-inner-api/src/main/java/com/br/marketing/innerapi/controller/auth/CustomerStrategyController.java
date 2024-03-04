package com.br.marketing.innerapi.controller.auth;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ClusterEnum;
import com.br.marketing.common.utils.StringUtils;
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
    private static final String STRATEGY_DISTRIBUTION_LIST = "http://k8s.brapp.com/compass-api/api/strategy-distribution/strategy-customizer/distributeList?" +
            "apiCode={apiCode}&strategyCategory={strategyCategory}&distributeType={distributeType}&strategyType={strategyType}";

    @Value("${cluster.flag}")
    private String clusterConfig;

    @Value("${api.productManagement.url}")
    private String productUrl;

    @Value("${api.productManagement.productTypeUrl}")
    private String productTypeUrl;


    @GetMapping("distributeList")
    public JSONObject distributeList(String apiCode, String strategyCategory, String distributeType, String strategyType) {
        String enumName = ClusterEnum.CLUSTER_PROD_C.getName();
        String url = STRATEGY_DISTRIBUTION_LIST;
        if (StringUtils.isNotBlank(clusterConfig) && enumName.equals(clusterConfig)) {
             url = STRATEGY_DISTRIBUTION_LIST.replace("k8s.brapp.com","k8s-bak.brapp.com");
        }
        Map<String, Object> urlVariables = getStringObjectMap(apiCode, strategyCategory, distributeType, strategyType);
        String result = restTemplate.getForObject(url, String.class, urlVariables);
        return (JSONObject) JSONObject.parse(result);
    }

    //productChineseName、productName、secondTypeName、spreadStatus、version、versions
    @GetMapping("createView")
    public ApiResult<Map<String, Object>> createView() {
        String result = restTemplate.getForObject(productUrl, String.class);
        String productType = restTemplate.getForObject(productTypeUrl, String.class);
        JSONObject productTypeJson = (JSONObject) JSONObject.parse(productType);
        JSONArray productTypeData = productTypeJson.getJSONArray("data");
        JSONObject jsonObject = (JSONObject) JSONObject.parse(result);
        JSONArray datArray = jsonObject.getJSONArray("data");
        List<Map<String, Object>> dataList = new ArrayList<>();
        Set<String> productSet = new TreeSet<>();

        for (int i = 0; i < datArray.size(); i++) {
            Map<String, Object> productMap = new HashMap<>();
            String productionChineseName = datArray.getJSONObject(i).getString("productionChineseName");
            String productName = datArray.getJSONObject(i).getString("productionName");
            String productionTypeCode = datArray.getJSONObject(i).getString("productionTypeCode");
            String spreadStatus = datArray.getJSONObject(i).getString("spreadStatus");
            String version = datArray.getJSONObject(i).getString("version");
            productMap.put("productChineseName", productionChineseName);
            productMap.put("productName", productName);
            productMap.put("spreadStatus", Integer.valueOf(spreadStatus));
            productMap.put("versions", version);
            productMap.put("version", "");
            for (int j = 0; j < productTypeData.size(); j++) {
                JSONObject jsonObject1 = productTypeData.getJSONObject(j);
                String typeCode = jsonObject1.getString("typeCode");
                String secondType = jsonObject1.getString("secondType");
                if(productionTypeCode.equals(typeCode)){
                    productMap.put("secondTypeName", secondType);
                    productMap.put("secondTypeCode", typeCode);
                }
            }
            dataList.add(productMap);
            productSet.add(productName);

        }
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (String productSetName : productSet) {
            List<Map<String, Object>> versionList = new ArrayList<>();
            Map<String, Object> productMap = new HashMap<>();
            for (Map<String, Object> stringObjectMap : dataList) {
                Object productName = stringObjectMap.get("productName");
                if (productName.equals(productSetName)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("spreadStatus",stringObjectMap.get("spreadStatus"));
                    String version = stringObjectMap.get("versions").toString();
                    map.put("version",version);
                    versionList.add(map);
                    productMap = stringObjectMap;
                }
            }
            productMap.put("versions", versionList);
            resultList.add(productMap);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", resultList);
        return new ApiResult<Map<String, Object>>().success(resultMap);
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
