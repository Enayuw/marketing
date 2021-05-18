package com.br.marketing.api.task;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import com.br.marketing.api.entities.client.HxResult;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Future;

/**
 * 数据策略更新状态
 */
@Slf4j
public class DtbUpdateInvoker implements LoanTaskInvoker{

    /**
     * 构造函数
     * @param hxResultFuture 画像结果
     * @param strategyApiContext strategyApiContext上下文
     * @param strategyResult 策略结果
     */
    public DtbUpdateInvoker(Future<HxResult> hxResultFuture, StrategyApiContext strategyApiContext, StrategyResult strategyResult ){
        try{
            if(hxResultFuture!=null) {
                HxResult  hxResult = hxResultFuture.get();
                if (hxResult != null) {
                    JSONObject flag = hxResult.getFlag();
                    strategyResult.putFlag(flag);
                    String string = hxResult.toString();
                    JSONObject hxJson = JSONObject.parseObject(string);

                    strategyResult.setHxResult(hxJson);
                    Object jsonData = strategyApiContext.getReqData();
                    String replace = jsonData.toString().replace("[", "").replace("]", "");
                    log.info("jsonData--{}",jsonData);
                    strategyResult.setJsonData(JSONObject.parseObject(replace));
                    strategyApiContext.setStrategyResult(strategyResult);
                    strategyApiContext.setDone(true);
                }else{
                    strategyResult.setHxResult(null);
                    strategyApiContext.setStrategyResult(strategyResult);
                    strategyApiContext.setDone(true);
                }
            }else {
                strategyResult.setHxResult(null);
                strategyApiContext.setStrategyResult(strategyResult);
                strategyApiContext.setDone(true);
            }
        }catch (Exception e){
            strategyApiContext.setStrategyResult(strategyResult);
            strategyApiContext.setDone(true);
            log.error("查询画像失败 -- swiftNumber{}",strategyApiContext.getSwiftNumber(),e);
        }

    }
    @Override
    public Future invoker() {
        return null;
    }
}
