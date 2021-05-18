package com.br.marketing.api.bussinesses;

import com.br.marketing.api.StrategyEarlyWaringApiApplication;
import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 数据策略
 */
@Service
@Slf4j
public class StrategyDtbBussiness  {

    @Resource
    private AsyncStrategyDto asyncStrategyDto;

    /**
     * Put.
     *
     * @param strategyApiContext the strategy api context
     * @param result             the result
     */
    public void put(StrategyApiContext strategyApiContext, Result result){
        strategyApiContext.setBeanFactory(StrategyEarlyWaringApiApplication.ac.getBeanFactory());
        strategyApiContext.setSwiftNumber(result.getSwiftNumber());
        generateTask(strategyApiContext,result);
    }

    /**
     * Generate task.
     *
     * @param strategyApiContext the strategy api context
     * @param result             the result
     */
    public void generateTask(StrategyApiContext strategyApiContext, Result result){
        //目前只支持单条
        if (strategyApiContext.getParamList().size() == 1){
            StrategyResult singleResult = new StrategyResult();
            singleResult.setSwiftNumber(result.getSwiftNumber());
            ApiUserParam apiUserParam = strategyApiContext.getParamList().get(0);
            apiUserParam.setBatchId(UUID.randomUUID().toString());
            asyncStrategyDto.invokerDtbTask(strategyApiContext,apiUserParam,singleResult);
        }
    }

}
