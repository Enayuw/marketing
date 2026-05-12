package com.br.marketing.innerapi.controller.auth;

import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ClusterEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.strategy.customizer.StrategyCustomizerProductCatalogAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
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
@Tag(name = "策略调用", description = "strategy")
@RequestMapping("strategy-customizer")
public class CustomerStrategyController {
    @Resource
    RestTemplate restTemplate;
    @Resource
    private StrategyCustomizerProductCatalogAssembler strategyCustomizerProductCatalogAssembler;
    private static final String STRATEGY_DISTRIBUTION_LIST = "http://k8s.brapp.com/compass-api/api/strategy-distribution/strategy-customizer/distributeList?" +
            "apiCode={apiCode}&strategyCategory={strategyCategory}&distributeType={distributeType}&strategyType={strategyType}";

    @Value("${cluster.flag}")
    private String clusterConfig;

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
        List<Map<String, Object>> resultList = strategyCustomizerProductCatalogAssembler.buildGroupedProductCatalog();
        Map<String, Object> resultMap = new HashMap<>(2);
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
