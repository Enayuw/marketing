package com.br.marketing.api.utils;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;

/**
 * Created by Bairong on 2019/8/26.
 */
public class ReturnUtil {

    /**
     * 返参封装
     * @param context 上下文
     * @param result 返回结果
     */
    public static void returnError(StrategyApiContext context,Result result){
        Object reqData = context.getReqData();
        String replace = reqData.toString().replace("[", "").replace("]", "");
        StrategyResult strategyResult = new StrategyResult(result.getData());
        strategyResult.setJsonData(JSONObject.parseObject(replace));
        context.setStrategyResult(strategyResult);
        context.setDone(true);
    }

}
