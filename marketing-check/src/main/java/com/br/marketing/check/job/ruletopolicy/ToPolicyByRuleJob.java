package com.br.marketing.check.job.ruletopolicy;

import com.br.marketing.check.service.XieChengCollidingService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.CustomerInfoPushMain;
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

    @Autowired
    XieChengCollidingService xieChengCollidingService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        Boolean actionMark = Boolean.TRUE;
        Result<Boolean> booleanResult;
        while (actionMark){
            Result<CustomerInfoPushMain> pushTask = pushRuleService.getPushTask();
            if(!ResultCode.SUCCESS.getValue().equals(pushTask.getCode())){
                actionMark = Boolean.FALSE;
                continue;
            }
            CustomerInfoPushMain pushTaskData= pushTask.getData();
            Result canPushTask = pushRuleService.isCanPushTask(pushTaskData.getId());
            if(ResultCode.SUCCESS.getValue().equals(canPushTask.getCode())){
                if(pushTaskData.getFilterType().equals("0")) {
                    booleanResult = pushRuleService.consumerPushCustomer(pushTaskData.getId());
                }else{
                    //携程撞库数据推决策
                    booleanResult = xieChengCollidingService.collidingDataPushPolicy(pushTaskData.getId());
                }
                if(ResultCode.SUCCESS.getValue().equals(booleanResult.getCode())&&Boolean.TRUE.equals(booleanResult.getData())){
                    log.warn(pushTask.getData()+":推送决策成功");
                }
            }
        }
    }
}
