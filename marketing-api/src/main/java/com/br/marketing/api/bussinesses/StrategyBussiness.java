package com.br.marketing.api.bussinesses;

import com.br.marketing.api.StrategyEarlyWaringApiApplication;
import com.br.marketing.api.broker.LoanStrategyExpression;
import com.br.marketing.api.entities.api.ApiUserParam;
import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import com.br.marketing.api.entities.api.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/** 策略API业务逻辑层
 * @author Wang Weiwei
 * @since 2018/3/12
 */
@Service
@Slf4j
public class StrategyBussiness  {

    @Resource
    private AsyncStrategyDto asyncStrategyDto;
    /**
     * 集群节点标识码
     */
    @Value("${cluster.flag}")
    private String flag;
    /**
     * 策略信息推送接口
     * 该接口只接受被正确处理过的消息，详细参数校验，权限校验，策略内容获取请详见各个切面
     * @param strategyApiContext 策略上下文
     * @param result 策略处理结果
     * */
    public void put(StrategyApiContext strategyApiContext, Result result) {
        strategyApiContext.setBeanFactory(StrategyEarlyWaringApiApplication.ac.getBeanFactory());
        strategyApiContext.setSwiftNumber(result.getSwiftNumber());
        // 解释器解释计算请求单元
        LoanStrategyExpression expression = new LoanStrategyExpression(strategyApiContext);
        expression.interpret();
        generateTask(strategyApiContext, result);
    }


    /**
     * 风险策略任务提交
     * @param strategyApiContext
     * @param result
     */
    private void generateTask(StrategyApiContext strategyApiContext, Result result) {
            StrategyResult singleResult = new StrategyResult();
            singleResult.setSwiftNumber(result.getSwiftNumber());
            ApiUserParam apiUserParam = strategyApiContext.getParamList().get(0);
            asyncStrategyDto.invokerTask(strategyApiContext, apiUserParam,singleResult, result);
    }
}
