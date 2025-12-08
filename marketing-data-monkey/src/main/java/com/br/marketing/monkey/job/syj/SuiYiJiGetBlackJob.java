package com.br.marketing.monkey.job.syj;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.monkey.service.syj.SuiYiJiBlackService;
import com.br.marketing.service.Impl.JobManager;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;


/**
 * @Author zhen.Li1
 * @Date 2025/12/4
 */
@Component
@Slf4j
public class SuiYiJiGetBlackJob extends AbstractSimpleElasticJob {


    @Resource
    private SuiYiJiBlackService suiYiJiBlackService;

    @Resource
    private JobManager jobManager;

    private final static String TITLE = "【随意记获取黑名单推转化】";


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        String apiCode;
        String jobParameter = shardingContext.getJobParameter();
        if (StringUtils.isNotBlank(jobParameter)) {
            apiCode = jobParameter;
        } else {
            apiCode = "3710222";
        }
        long start = System.currentTimeMillis();
        String actionDate = LocalDate.now().toString();
        int actionType = JobManager.ActionTypeEnum.SUIYIJI_GETBLACK_PUSHTRANSFER.getActionType();
        TransferActionFront action = jobManager.getFrontData(apiCode, actionDate, actionType, null);
        if (action != null) {
            Integer actionStatus = action.getStatus();
            log.warn(TITLE + "任务执行记录已存在, apiCode:{}, actionDate:{}, actionStatus:{}", apiCode, actionDate, actionStatus);
            return;
        }

        action = jobManager.saveFront(apiCode, actionDate, actionType);
        if (action.getId() == null) {
            log.warn(TITLE + "任务执行记录新增失败, apiCode:{}, actionDate:{}", apiCode, actionDate);
            return;
        }
        suiYiJiBlackService.blackPushTransfer(apiCode);

        //执行完成
        jobManager.updateFrontDataStatus(action.getId(), 2);
        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end - start);
    }
}
