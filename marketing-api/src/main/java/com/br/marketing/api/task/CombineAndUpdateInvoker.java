package com.br.marketing.api.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.StrategyEarlyWaringApiApplication;
import com.br.marketing.api.client.ReturnDataRepository;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.entities.client.*;
import com.br.marketing.api.entity.Strategy;
import com.br.marketing.common.constants.auth.AuthShowProductor;
import com.br.marketing.common.constants.web.ResponseCode;
import com.br.marketing.common.exception.strategy.LoanTaskException;
import com.br.marketing.common.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 合并计算
 */
@Slf4j
public class CombineAndUpdateInvoker implements LoanTaskInvoker<StrategyResult>{
    private StrategyApiContext strategyApiContext;
    private JSONObject flag ;

    /**
     * 合并计算
     * @param strategyApiContext 上下文
     * @param strategyResult 策略结果
     * @param ruleResult 规则结果
     * @param hxResultFuture 画像结果
     * @param loanRetrialResult  规则引擎返回结果
     * @param behaviorScoreResult 行为评分结果
     * @param sanxiangzhiliResultFuture 三相结果
     */
    public CombineAndUpdateInvoker(StrategyApiContext strategyApiContext, StrategyResult strategyResult, Future<RuleTaskResult> ruleResult,
                                   Future<HxResult> hxResultFuture,
                                   Future<LoanRetrialResult> loanRetrialResult, Future<BehaviorScoreResult> behaviorScoreResult,
                                   Future<List<SanxiangzhiliResult>> sanxiangzhiliResultFuture ) {
        this.strategyApiContext = strategyApiContext;
        flag = strategyResult.getFlag();
        List<SanxiangzhiliResult> sanxiangzhiliResultList=null;
        if(sanxiangzhiliResultFuture!=null){
            try {
                sanxiangzhiliResultList = sanxiangzhiliResultFuture.get(6, TimeUnit.SECONDS);
            }catch (Exception e){
                log.error("三相之力结果异常,swiftNumber--{}",strategyApiContext.getSwiftNumber(),e);
            }
        }
        addHxFlag(strategyResult, hxResultFuture, sanxiangzhiliResultList);
        merge(strategyResult, ruleResult, hxResultFuture, loanRetrialResult, behaviorScoreResult);
    }
    /**
     * 在策略总flag中，添加画像的flag
     * 添加完成画像的flag后，再依照画像flag来设置策略总flag
     *
     * 策略总flag生成规范，只有所有的产品都错误(为空)，总flag才错误(为空)
     *
     * flag 取值的规范如下:
     * 1(输出成功),0(未匹配上无输出),98(用户输入信息不足),99(系统异常)
     * */
    private void addHxFlag(StrategyResult strategyResult, Future<HxResult> hxResultFuture, List<SanxiangzhiliResult> sanxiangzhiliResultList) {

            HxResult hxResult = null;
            if (hxResultFuture != null) {
                try {
                    hxResult = hxResultFuture.get();
                }catch (Exception e){
                    log.error("画像结果异常,swiftNumber--{}",strategyApiContext.getSwiftNumber(),e);
                }
            }
            try {
                JSONObject hxFlag = new JSONObject();

                if (hxResult != null) {
                    hxFlag = hxResult.getFlag();
                }
                if (sanxiangzhiliResultList != null) {
                    for (SanxiangzhiliResult sanxiangzhiliResult : sanxiangzhiliResultList) {
                        JSONObject data = sanxiangzhiliResult.getData();
                        if(data!=null&&!data.isEmpty()&&data.containsKey("flag")){
                            JSONObject sxFlag = data.getJSONObject("flag");
                            for(String sxkey:sxFlag.keySet()){
                                String value = sxFlag.getString(sxkey);
                                sxkey=sxkey.replace("flag_","");
                                flag.put(sxkey,value);
                            }
                        }
                    }
                }
                String defaultFlag = "99";
                for (String hxKey : hxFlag.keySet()) {
                    flag.put(hxKey, hxFlag.getString(hxKey));
                    if ("0".equals(hxFlag.getString(hxKey)) && "99".equals(defaultFlag)) {
                        defaultFlag = "0";
                    } else if (!"1".equals(defaultFlag) && "1".equals(hxFlag.getString(hxKey))) {
                        defaultFlag = "1";
                    }
                }


            } catch (Exception e) {
                throw new LoanTaskException("规则任务无法获取画像返回结果", e);
            }
            strategyResult.putFlag(flag);
    }
    /**
     * 合并入库
     * @param ruleTaskResultFuture 规则任务返回结果
     * @param strategyResult 规则结果
     * @param loanRetrialResult 贷前重审结果
     * @param behaviorScoreResult 行为评分结果
     */
    private void merge(StrategyResult strategyResult, Future<RuleTaskResult> ruleTaskResultFuture, Future<HxResult> hxResultFuture,
                       Future<LoanRetrialResult> loanRetrialResult, Future<BehaviorScoreResult> behaviorScoreResult) {
        HxResult hxResult = null;
        LoanRetrialResult retrialResult;
        BehaviorScoreResult scoreResult;
        //计算合并逻辑
        try{
            boolean isRight = false;
            if(hxResultFuture!=null && hxResultFuture.get()!=null){
                try {
                    hxResult = hxResultFuture.get();
                }catch (Exception e){
                    log.error("画像结果异常--{}  swiftNumber--{}",e,strategyApiContext.getSwiftNumber());
                }
            }
            JSONObject strategy = strategyResult.getLoanStrategy();

            if(ruleTaskResultFuture!=null){
                if(ruleTaskResultFuture.get()!=null){
                    isRight = true;
                    RuleTaskResult ruleTaskResult = ruleTaskResultFuture.get();
                    strategyResult = ruleTaskResult.getStrategyResult();
                    strategy = strategyResult.getLoanStrategy();
                }else{
                    strategy.put("ruleFinalRisk","Exception");
                    strategyResult.addFlag("loanStrategy", "99");
                }
            }

            if(loanRetrialResult != null){
                if(loanRetrialResult.get()!=null){
                    retrialResult = loanRetrialResult.get();
                    if(retrialResult.isSuccess()){
                        strategyResult.addFlag("reviewStrA", "1");
                        isRight = true;
                        strategy.put("retryRisk",retrialResult.getRetryRisk());
                    }else{
                        strategyResult.addFlag("reviewStrA","99");
                        strategy.put("retryRisk","Exception");
                    }
                }else{
                    strategy.put("retryRisk","Exception");
                    strategyResult.addFlag("reviewStrA","99");
                }
            }

            if(behaviorScoreResult!=null){
                if(behaviorScoreResult.get()!=null){
                    scoreResult = behaviorScoreResult.get();
                    if(scoreResult.isSuccess()){
                        isRight = true;
                        strategy.put("behaviorRisk",scoreResult.getBehaviorRisk());
                        strategyResult.addFlag("behaviorScore","1");
                    }else {
                        strategy.put("behaviorRisk","Exception");
                        strategyResult.addFlag("behaviorScore","99");
                    }
                }else {
                    strategy.put("behaviorRisk","Exception");
                    strategyResult.addFlag("behaviorScore","99");
                }
            }

            riskLevel(strategy);

            strategyToEntity(strategyResult, hxResult,isRight);
            // log.info("更新数据库耗时：{}", System.currentTimeMillis()-begin);
        }catch (Exception e){
            log.error("合并任务失败{}  swiftNumber--{}",e,strategyApiContext.getSwiftNumber());
            try {
                strategyToEntity(strategyResult, hxResult,false);
            }catch (Exception e2){
                strategyApiContext.setStrategyResult(strategyResult);
                strategyApiContext.setDone(true);
                log.error("合并任务失败{} swiftNumber--{}",e2,strategyApiContext.getSwiftNumber());
                e.printStackTrace();
            }
        }
    }

    /**
     * 最终结果 A B C D 无结果
     * @param strategy strategy
     */
    private void riskLevel(JSONObject strategy){
        log.info("计算最终风险等级：{}",JSON.toJSONString(strategy));
        String riskLevel = "无结果";
        String ruleRisk = strategy.getString("ruleFinalRisk");
        if(StringUtils.isNotEmpty(ruleRisk)&&compare(ruleRisk,riskLevel)){
            riskLevel = ruleRisk;
        }
        String retryRisk = strategy.getString("retryRisk");
        if(StringUtils.isNotEmpty(retryRisk)&&compare(retryRisk,riskLevel)){
            riskLevel = retryRisk;
        }
        String scoreRisk = strategy.getString("behaviorRisk");
        if(StringUtils.isNotEmpty(scoreRisk)&&compare(scoreRisk,riskLevel)){
            riskLevel = scoreRisk;
        }
        strategy.put("strategyDecision",riskLevel);
        log.info("最终风险等级为:{}",riskLevel);
    }

    /**
     * 策略定义
     * @param strategyResult strategyResult
     * @param strategyApiContext strategyApiContext
     */
    private void strategyDefine(StrategyResult strategyResult,StrategyApiContext strategyApiContext) {
        JSONObject loanStrategy = strategyResult.getLoanStrategy();
        Strategy strategy = strategyApiContext.getStrategy();
        loanStrategy.put("strategyId", strategy.getStrCode());
        loanStrategy.put("strategyName", strategy.getStrName());
        loanStrategy.put("version", strategy.getUseVersion());
        loanStrategy.put("customerType", strategy.getCustomerType());
        loanStrategy.put("prodType", strategy.getProdType());
    }

    /**
     * 比较结果
     * @param a a
     * @param b b
     * @return true|false
     */
    private boolean compare(String a, String b){
        return Constants.riskMap.get(a)>Constants.riskMap.get(b);
    }

    /**
     * 将策略的结果转化为策略序列化实体
     * <p>
     */
    private void strategyToEntity(StrategyResult strategyResult, HxResult hxResult, boolean isRight) {
        ReturnDataRepository returnDataRepository= StrategyEarlyWaringApiApplication.ac.getBean(ReturnDataRepository.class);
        //log.info("将策略的结果转化为策略序列化实体：isRight:{}",isRight);
        if (isRight) {
            strategyDefine(strategyResult, strategyApiContext);
            strategyResult.putCode(ResponseCode.SUCC);
            strategyResult.addFlag("loanStrategy", "1");
        } else {
            strategyResult.putCode(ResponseCode.ERR_SYSTEM);
            strategyResult.addErrorFlag();
        }
        //判断画像是否有返回，以及调用画像的次数。
        if(AuthShowProductor.SHOW==strategyApiContext.getIsShowData()
            &&hxResult != null&&StringUtils.isEmpty(hxResult.getBehaviorResult())){
            JSONObject jsonObject = JSONObject.parseObject(hxResult.toString());
            JSONObject hxJson=new JSONObject();
            hxJson.putAll(jsonObject);
            returnDataRepository.hxReturnDataDetail(hxJson,strategyApiContext.getApiCode()
                    ,strategyResult,strategyApiContext.getStrategy());
            strategyResult.setHxResult(hxJson);

        }
        Object jsonData = strategyApiContext.getReqData();
        Strategy strategy = strategyApiContext.getStrategy();
        String replace = jsonData.toString().replace("[", "").replace("]", "");
        JSONObject jsonObject = JSONObject.parseObject(replace);
        jsonObject.put("strategyJson",JSONObject.toJSON(strategy));
        strategyResult.setJsonData(jsonObject);
        strategyApiContext.setStrategyResult(strategyResult);
        strategyApiContext.setDone(true);

    }

    @Override
    public Future<StrategyResult> invoker() {
        return null;
    }
}
