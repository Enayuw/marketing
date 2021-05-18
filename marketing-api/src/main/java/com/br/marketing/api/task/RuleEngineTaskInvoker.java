package com.br.marketing.api.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.broker.RuleProductorsExpression;
import com.br.marketing.api.client.RuleEngineClient;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.client.HxResult;
import com.br.marketing.api.entities.client.RuleResult;
import com.br.marketing.api.entities.client.SanxiangzhiliResult;
import com.br.marketing.api.service.AlarmCs;
import com.br.marketing.common.exception.strategy.LoanTaskException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/** 规则引擎任务执行器
 * @author Wang Weiwei
 * @since 2018/3/17
 */
@Slf4j
public class RuleEngineTaskInvoker extends BaseRetryTaskInvoker<RuleResult> implements RuleProductorsExpression {

    private ThreadPoolTaskExecutor threadPoolTaskExecutor;
    private RuleEngineClient ruleEngineClient;
    private JSONObject param = new JSONObject();
    private String rescode;
    private String flag;
    private JSONObject  productJson;
    private HxResult hxResult;
    private AlarmCs alarmCs;

    /**
     * 规则引擎任务执行器
     * @param hxResult 画像结果
     * @param sanxiangzhiliResultList 三相结果
     * @param strategyApiContext 上下文
     */
    public RuleEngineTaskInvoker(HxResult hxResult, List<SanxiangzhiliResult> sanxiangzhiliResultList, StrategyApiContext strategyApiContext) {
        super();
        this.hxResult=hxResult;
        BeanFactory beanFactory = strategyApiContext.getSpringContext();
        threadPoolTaskExecutor = beanFactory.getBean("ThreadPool", ThreadPoolTaskExecutor.class);
        ruleEngineClient = beanFactory.getBean("ruleEngineClient", RuleEngineClient.class);
        alarmCs=  beanFactory.getBean("alarmCS1", AlarmCs.class);
        rePackageParam(strategyApiContext.getApiCode(),sanxiangzhiliResultList);
        parseRuleProductors(strategyApiContext.getStrategy().getRuleType());
    }


    /**
     * 构建规则引擎级请求参数
     * */
    private void rePackageParam(String apiCode, List<SanxiangzhiliResult> sanxiangzhiliResultList) {
        // 等待画像请求完成，否则让步执行
        try {
            JSONObject hxData =new JSONObject();
            param.put("api_code", apiCode);
            if(hxResult!=null){
                hxData = hxResult.getData();
               // removeOthers(hxData);
            }
            JSONObject purchaseDatas =new JSONObject();
            if(hxData!=null&&!hxData.isEmpty()){
                purchaseDatas.putAll(hxData);
            }
            if (sanxiangzhiliResultList!=null){
                for (SanxiangzhiliResult sanxiangzhiliResult : sanxiangzhiliResultList) {
                    JSONObject sxData = sanxiangzhiliResult.getData();
                    rescode = sxData.containsKey("code") ? sxData.getString("code") : "-1";
                    if ("600000".equals(rescode) && sxData.containsKey("product")
                            && !sxData.getJSONObject("product").isEmpty()) {
                        flag = sxData.getJSONObject("flag").keySet().iterator().next();
                        productJson = sxData.getJSONObject("product");
                        log.info("三相之力产品返回--{}",productJson);
                        if ("flag_badinfo".equals(flag)) {
                            purchaseDatas.put("BadInfo", productJson);
                        }
                        if ("flag_courtdetail".equals(flag)) {
                            purchaseDatas.put("CourtDetail", productJson);
                        }
                    }
                    if("600002".equals(rescode)){
                        alarmCs.handle(new Exception("调用三方系统错误（数据源返回欠费等敏感信息) --- {}"+sxData),"api","loan-strategy-api");
                    }
                    if("600003".equals(rescode)){
                        alarmCs.handle(new Exception("调用三方套餐配置错误  --- {}"+sxData),"api","loan-strategy-api");
                    }
                    if("600005".equals(rescode)){
                        alarmCs.handle(new Exception("调用三方超时  --- {}"+sxData),"api","loan-strategy-api");
                    }
                }
        }
            log.info("规则引擎purchaseDatas--{}",hxData);
            param.put("purchaseDatas", purchaseDatas);
        } catch (Exception e) {
            throw new LoanTaskException("规则任务无法获取画像返回结果", e);
        }
    }

    @Override
    Future<RuleResult> retryInvoker() {
        return threadPoolTaskExecutor.submit(new Callable<RuleResult>() {
            @Override
            public RuleResult call() {
                RuleResult ruleResult = ruleEngineClient.query1(param);
                return ruleResult;
            }
        });
    }


    /**
     * 构建要调用的规则集合调用方式的参数
     * */
    @Override
    public final void parseRuleProductors(String ruleType) {
        // 给规则引擎的多版本规则列表
        JSONArray ruleList = new JSONArray();
        // 策略定义中的规则列表
        JSONArray ruleArray = JSONArray.parseArray(ruleType);
        for (int i = 0; i < ruleArray.size(); i++) {
            JSONObject object = ruleArray.getJSONObject(i);
            StringBuilder hxproductors = new StringBuilder();
            hxproductors.append(object.getString("ruleType"))
            .append("#")
            .append(object.getString("version"));
            ruleList.add(hxproductors.toString());
        }
        param.put("ruleList",ruleList);
    }
}
