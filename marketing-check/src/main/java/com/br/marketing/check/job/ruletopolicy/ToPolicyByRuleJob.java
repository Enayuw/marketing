package com.br.marketing.check.job.ruletopolicy;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.service.PushRuleService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 规则推送决策
 */
@Component
@Slf4j
public class ToPolicyByRuleJob extends AbstractSimpleElasticJob {


    @Autowired
    PushRuleService pushRuleService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Boolean actionMark = Boolean.TRUE;
        while (actionMark){
            Result<Long> pushTask = pushRuleService.getPushTask();
            if(!ResultCode.SUCCESS.getValue().equals(pushTask.getCode())){
                actionMark = Boolean.FALSE;
                continue;
            }
            Result canPushTask = pushRuleService.isCanPushTask(pushTask.getData());
            if(ResultCode.SUCCESS.getValue().equals(canPushTask.getCode())){
                Result<Boolean> booleanResult = pushRuleService.consumerPushCustomer(pushTask.getData());
                if(ResultCode.SUCCESS.getValue().equals(booleanResult.getCode())&&Boolean.TRUE.equals(booleanResult.getData())){
                    log.warn(pushTask.getData()+":推送决策成功");
                }
            }
        }
    }
}
