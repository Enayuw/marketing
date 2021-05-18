package com.br.marketing.api.aspect.strategy.batch.put;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.aspect.AroundAspectProceeding;
import com.br.marketing.api.client.DtbStrategyClient;
import com.br.marketing.api.client.StrategyClient;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entity.RiskRank;
import com.br.marketing.api.entity.Strategy;
import com.br.marketing.api.utils.ReturnUtil;
import com.br.marketing.common.constants.web.ResponseCode;
import com.br.marketing.common.exception.strategy.LoanStrategyException;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/** 策略检验器
 * @author Wang Weiwei
 * @since 2018/3/15
 */
@Aspect
@Component
@Order(130)
@Slf4j
public class StrategyValidator implements AroundAspectProceeding {
    @Resource
    private DtbStrategyClient dtbStrategyClient;

    @Around("com.br.marketing.api.aspect.strategy.StrategyBussinessJoinPoint.put()")
    @Override
    public void proceeding(ProceedingJoinPoint joinPoint) throws Throwable {
        Paramter paramter = new Paramter(joinPoint).invoke();
        StrategyApiContext context = paramter.getContext();
        Result result = paramter.getResult();
        if(context.getStrategyId().startsWith("STRB")){
            try {
                String str = StrategyClient.getStrategy(context.getApiCode(), context.getStrategyId());
                Strategy strategy=null;
                if(StringUtils.isNotEmpty(str)){
                     strategy = JSONObject.parseObject(str, Strategy.class);
                }
                if (strategy == null){
                    result.putCode(ResponseCode.MISS_STRATEGR_ID);
                    ReturnUtil.returnError(context,result);
                    return;
                }else {
                    if("0".equals(strategy.getCanUse())){
                        //设置策略
                        setStrategy(context,strategy);
                        setStrategyRuleType(strategy);
                    }else {
                        // log.error("策略贷中状态为不可用:--{}",strategy.toString());
                        result.putCode(ResponseCode.ERROR_PERMISSION_STATUS);
                        ReturnUtil.returnError(context,result);
                        return;
                    }
                }
            } catch (LoanStrategyException e) {
                log.warn("LoanStrategyException",e);
                // 策略全部停用
                result.putCode(ResponseCode.STRATEGY_STOPED);
                ReturnUtil.returnError(context,result);
                return;
            }
            String string = StrategyClient.getRiskRank(context.getApiCode());
            JSONObject jsonObject = JSONObject.parseObject(string);
            RiskRank riskRank=new RiskRank();
            riskRank.setLogic(jsonObject.getString("ruleType"));
            context.setRiskRank(riskRank);
            joinPoint.proceed();
        }else if(context.getStrategyId().startsWith("DTB")){
            //远程调用数据策略配置
            try {
                String str = dtbStrategyClient.getStrategy(context.getApiCode(), context.getStrategyId());
                if (StringUtils.isEmpty(str)) {
                    result.putCode(ResponseCode.MISS_DTB_STRATEGR_ID);
                    ReturnUtil.returnError(context,result);
                    return;
                } else {
                    JSONObject object = JSONObject.parseObject(str);
                    log.info("数据策略配置：{}",JSON.toJSONString(object));
                    if("000000".equals(object.getString("code"))){
                        JSONObject data = object.getJSONObject("data");
                        if(data!=null && StringUtils.isNotEmpty(data.getString("dataProdList"))
                                && data.getInteger("status")==1 && data.getInteger("canUse")==1){
                            JSONObject dtbStrategy = object.getJSONObject("data");
                            JSONArray dtbStrategyArray=new JSONArray();
                            JSONArray jsonArray = JSON.parseObject(dtbStrategy.getString("dataProdList")).getJSONArray("dataProdList");
                            if(jsonArray.toJSONString().indexOf("BadInfo")!=-1||
                                    jsonArray.toJSONString().indexOf("CourtDetail")!=-1){
                                for(int i=0;i<jsonArray.size();i++){
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    if(!"BadInfo".equals(jsonObject.getString("code"))&&
                                            !"CourtDetail".equals(jsonObject.getString("code"))){
                                        dtbStrategyArray.add(jsonObject);
                                    }
                                }
                            }else {
                                dtbStrategyArray= jsonArray;
                            }
                            //log.info("dtbStrategyArray---{}",dtbStrategyArray);
                            if(dtbStrategyArray.size()==0){
                                result.putCode(ResponseCode.DTB_STRATEGR_ERROR);
                                ReturnUtil.returnError(context,result);
                                return;
                            }
                            dtbStrategy.put("dataProdList", dtbStrategyArray);
                            context.setDtbStrategy(dtbStrategy);
                            joinPoint.proceed();
                        }else{
                            result.putCode(ResponseCode.DTB_STRATEGR_ERROR);
                            ReturnUtil.returnError(context,result);
                            return;
                        }
                    }else{
                        result.putCode(ResponseCode.DTB_STRATEGR_ERROR);
                        ReturnUtil.returnError(context,result);
                        return;
                    }
                }
            }catch (Exception e){
                log.error("远程调用数据策略配置异常---",e);
                // 策略全部停用
                result.putCode(ResponseCode.STRATEGY_STOPED);
                ReturnUtil.returnError(context,result);
                return;
            }
        }else{
            result.putCode(ResponseCode.ERROR_STRATEGR_ID);
            ReturnUtil.returnError(context,result);
            return;
        }
    }

    /**
     * 设置策略
     * @param context
     * @param strategy
     */
    private void setStrategy(StrategyApiContext context,Strategy strategy){
        //log.info("策略内容----{}-------{}",strategy.getRuleType());
        if (!StringUtils.isEmpty(strategy.getRuleType())){
            if(strategy.getRuleType().startsWith("{")){
                JSONObject ruleObject = JSONObject.parseObject(strategy.getRuleType());
                if (ruleObject != null && 1 == ruleObject.getInteger("status") && ruleObject.getJSONArray("ruleTypeList") != null){
                    context.setHaveRule(Boolean.TRUE);
                    //判断是含有三相之力的任务
                        JSONArray ruleTypeList = ruleObject.getJSONArray("ruleTypeList");
                        JSONArray  array=new JSONArray();
                        for(int i=0;i<ruleTypeList.size();i++){
                            JSONObject jsonObject = ruleTypeList.getJSONObject(i);
                            if("Rule_W_BadInfo_mix".equals(jsonObject.getString("ruleType"))||
                                    "Rule_W_CourtDetail_mix".equals(jsonObject.getString("ruleType"))){
                                context.setHaveSanxiangzhili(Boolean.TRUE);
                            }else{
                                array.add(jsonObject);
                            }
                        }
                        if(array.size()>0){
                            context.setHaveHx(Boolean.TRUE);
                        }
                }
            }else if(strategy.getRuleType().startsWith("[")){
                context.setHaveRule(Boolean.TRUE);
                //判断是含有三相之力的任务
                String ruleType = strategy.getRuleType();
                JSONArray ruleTypeArray=JSONArray.parseArray(ruleType);
                JSONArray  array=new JSONArray();
                for(int i=0;i<ruleTypeArray.size();i++){
                    JSONObject ruleTypeJson = ruleTypeArray.getJSONObject(i);
                    if("Rule_W_BadInfo_mix".equals(ruleTypeJson.getString("ruleType"))||
                            "Rule_W_CourtDetail_mix".equals(ruleTypeJson.getString("ruleType"))){
                        context.setHaveSanxiangzhili(Boolean.TRUE);
                    }else{
                        array.add(ruleTypeJson);
                    }
                }
                if(array.size()>0){
                    context.setHaveHx(Boolean.TRUE);
                }
            }

        }
        if(!StringUtils.isEmpty(strategy.getStrategyRetry())){
            //贷前重审条数
            JSONObject review = JSONObject.parseObject(strategy.getStrategyRetry());
            if(review!=null && 1==review.getInteger("status") && review.getJSONArray("preLoanStrategy")!=null){
                context.setHaveReview(Boolean.TRUE);
            }
        }
        if(!StringUtils.isEmpty(strategy.getBehaviorScore())){
            JSONObject behavior = JSON.parseObject(strategy.getBehaviorScore());
            if(behavior!=null && 1==behavior.getInteger("status") && behavior.getJSONArray("behaviorScore")!=null){
                context.setHaveBehavior(Boolean.TRUE);
                context.setHaveHx(Boolean.TRUE);
            }
        }
        context.setStrategy(strategy);
    }

    /**
     * 解析规则集
     * @param strategyRuleType
     */
    private void setStrategyRuleType(Strategy strategyRuleType) {
        String ruleType = strategyRuleType.getRuleType();
        // log.info("解析规则集--{}",strategyRuleType.getRuleType());
        if(StringUtils.isNotEmpty(ruleType)){
            if(ruleType.startsWith("{")&&ruleType.endsWith("}")){
                //因为用的@Around注解，如果直接使用Strategy对象中的RuleType属性去设置规则集，有可能进入到下面方法时RuleType属性已经发生改变
                newStrategy(strategyRuleType,ruleType);
            }else if(ruleType.startsWith("[")&&ruleType.endsWith("]")){
                 JSONArray.parseArray(strategyRuleType.getRuleType());
            }else{
                log.error("策略规则识别失败--{}",ruleType);
            }
        }
    }

    private void newStrategy(Strategy strategyRuleType,String ruleType) {
        JSONObject ruleObject = JSONObject.parseObject(ruleType);
        if (ruleObject != null && 1 == ruleObject.getInteger("status") && ruleObject.getJSONArray("ruleTypeList") != null){
            strategyRuleType.setRuleType(ruleObject.getJSONArray("ruleTypeList").toJSONString());
        }else {
            strategyRuleType.setRuleType("");
        }
    }
}
