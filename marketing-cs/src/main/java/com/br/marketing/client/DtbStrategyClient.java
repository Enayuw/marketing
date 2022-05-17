package com.br.marketing.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 规则引擎访问客户端，通过ribbon调用规则引擎
 *
 * @author Wang Weiwei
 * @since 2018/3/19
 */
@Service
@Slf4j
public class DtbStrategyClient {

    @Resource
    RestTemplate restTemplate;

    /**
     * 远程调用数据策略接口，
     * <p>
     * 返回例子：
     * {
     * "message":"成功",
     * "data":{
     * "apiCode":"4000861",
     * "canUse":1,
     * "createTime":1535698020000,
     * "createUser":"李博文测试",
     * "dataProdList":"{"dataProdList":[{"code":"KeyAttribution","version":"V1.0"},
     * {"code":"SpecialList_c","version":"V1.0"},{"code":"TotalLoan","version":"V1.0"}
     * ,{"code":"Stability_c","version":"V2.0"},{"code":"Consumption_c","version":"V2.0"}
     * ,{"code":"ApplyLoanStr","version":"V2.0"},{"code":"InfoRelation","version":"V2.0"}
     * ,{"code":"ApplyLoan_d","version":"V2.0"},{"code":"ApplyLoanInterval","version":"V1.0"}
     * ,{"code":"ApplyLoanMon","version":"V2.0"},{"code":"Media_c","version":"V2.0"}]}",
     * "datastrCode":"DTB0000013",
     * "datastrDesc":"但是",
     * "datastrName":"测试",
     * "id":626,
     * "prodName":"测试",
     * "prodType":"100081",
     * "status":1,
     * "type":6,
     * "updateTime":1535698020000,
     * "userType":"早期逾期客户"
     * },
     * "code":"000000"
     * }
     */
    public String getStrategy(String apiCode, String strategyId) {
        /*HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpRequestFactory.setConnectionRequestTimeout(3000);
        httpRequestFactory.setConnectTimeout(1000);
        httpRequestFactory.setReadTimeout(5000);
        RestTemplate restTemplate =new RestTemplate(httpRequestFactory);*/
        Map<String, Object> urlVariables = new HashMap<>();
        urlVariables.put("apiCode", apiCode);
        urlVariables.put("code", strategyId);
        String result = "";
//        String url = "http://strategycenter-service/dataStrategy/getDtbByCode?apiCode={apiCode}&code={code}";
        try {
            result = restTemplate.getForObject("http://k8s.brapp.com/compass-api/api/strategycenter-service/" +
                    "dataStrategy/getDtbByCode?apiCode={apiCode}&code={code}", String.class, urlVariables);
//            result = restTemplate.getForObject(url, String.class, urlVariables);
        } catch (Exception e) {
            log.warn("调用数据策略错误", e);
            try {
                result = restTemplate.getForObject("http://k8s.brapp.com/compass-api/api/strategycenter-service" +
                        "/dataStrategy/getDtbByCode?apiCode={apiCode}&code={code}", String.class, urlVariables);
//                result = restTemplate.getForObject(url, String.class, urlVariables);
            } catch (Exception e1) {
                log.error("调用数据策略重试错误", e1);
            }
        }
        return result;
    }


    public void needReturnProduct(String apiCode, String strategyId, Map<String, String> map) {
        String dtbstrategy = getStrategy(apiCode, strategyId);
        JSONObject dtbJson = JSONObject.parseObject(dtbstrategy);
        if (dtbJson != null && "000000".equals(dtbJson.getString("code"))) {
            JSONObject data = dtbJson.getJSONObject("data");
            if (data != null && "1".equals(data.getString("status")) && "1".equals(data.getString("canUse"))) {
                JSONObject dataProdList = data.getJSONObject("dataProdList");
                if (dataProdList != null && dataProdList.containsKey("dataProdList")) {
                    JSONArray dataProdList1 = dataProdList.getJSONArray("dataProdList");
                    for (int j = 0; j < dataProdList1.size(); j++) {
                        JSONObject jsonObject = dataProdList1.getJSONObject(j);
                        if (jsonObject != null && jsonObject.containsKey("code")) {
                            String code = jsonObject.getString("code");
                            String version = jsonObject.getString("version");
                            if (StringUtils.isNotEmpty(code)) {
                                code = code.toLowerCase();
                                map.put(code, version);
                            }
                        }
                    }
                }
            }
        }
    }

}