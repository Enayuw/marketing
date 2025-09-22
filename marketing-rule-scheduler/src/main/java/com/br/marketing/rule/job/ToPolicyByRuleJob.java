package com.br.marketing.rule.job;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.CustomerInfoPushMain;
import com.br.marketing.rule.service.XieChengCollidingService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.rulecenter.IRuleCenterPushService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 规则推送决策
 */
@Component
@Slf4j
public class ToPolicyByRuleJob extends AbstractSimpleElasticJob {


    @Resource
    PushRuleService pushRuleService;

    @Resource
    XieChengCollidingService xieChengCollidingService;

    @Resource
    IRuleCenterPushService iRuleCenterPushService;

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
                log.warn("推送决策业务开始" + "start");
                long start = System.currentTimeMillis();
                if (pushTaskData.getFilterType().equals(1)){
                    //携程撞库数据推决策
                    booleanResult = xieChengCollidingService.collidingDataPushPolicy(pushTaskData.getId());
                }else {
                    booleanResult = iRuleCenterPushService.pushData(pushTaskData.getId());
                }
                long end = System.currentTimeMillis();
                log.warn("推送决策业务结束" + "end, 耗时{}ms", end-start);
                if(ResultCode.SUCCESS.getValue().equals(booleanResult.getCode())&&Boolean.TRUE.equals(booleanResult.getData())){
                    log.warn(pushTask.getData()+":推送决策成功");
                }
            }
        }
    }
}
