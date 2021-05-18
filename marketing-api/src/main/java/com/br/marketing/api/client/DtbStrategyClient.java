package com.br.marketing.api.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/** 规则引擎访问客户端，通过ribbon调用规则引擎
 * @author Wang Weiwei
 * @since 2018/3/19
 */
@Service("dtbStrategyClient")
@Slf4j
public class DtbStrategyClient {

    @Resource
    private RestTemplate restTemplate;


    /**
     * Gets strategy.
     *
     *   远程调用数据策略接口，
     *
     *   返回例子：
     *   {
     *       "message":"成功",
     *       "data":{
     *           "apiCode":"4000861",
     *           "canUse":1,
     *           "createTime":1535698020000,
     *           "createUser":"李博文测试",
     *           "dataProdList":"{"dataProdList":[{"code":"KeyAttribution","version":"V1.0"},
     *           {"code":"SpecialList_c","version":"V1.0"},{"code":"TotalLoan","version":"V1.0"},{
     *           "code":"Stability_c","version":"V2.0"},{"code":"Consumption_c","version":"V2.0"},
     *           {"code":"ApplyLoanStr","version":"V2.0"},{"code":"InfoRelation","version":"V2.0"},
     *           {"code":"ApplyLoan_d","version":"V2.0"},{"code":"ApplyLoanInterval","version":"V1.0"},
     *           {"code":"ApplyLoanMon","version":"V2.0"},{"code":"Media_c","version":"V2.0"}]}",
     *           "datastrCode":"DTB0000013",
     *           "datastrDesc":"但是",
     *           "datastrName":"测试",
     *           "id":626,
     *           "prodName":"测试",
     *           "prodType":"100081",
     *           "status":1,
     *           "type":6,
     *           "updateTime":1535698020000,
     *           "userType":"早期逾期客户"
     *       },
     *       "code":"000000"
     *   }
     *
     *
     * @param apiCode    the api code
     * @param strategyId the strategy id
     * @return the strategy
     */
    public String getStrategy(String apiCode, String strategyId) {
        Map<String,Object> urlVariables = new HashMap<>();
        urlVariables.put("apiCode",apiCode);
        urlVariables.put("code",strategyId);
        log.info("开始调用数据策略配置服务--apiCode:{}---code：{}",apiCode,strategyId);
        String result = restTemplate.getForObject("http://strategycenter-service/dataStrategy/" +
                "getDtbByCode?apiCode={apiCode}&code={code}", String.class, urlVariables);
        return result;
    }


}