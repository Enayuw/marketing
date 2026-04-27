package com.br.marketing.monkey.job.didi;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.didi.DidiConstructDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
@Slf4j
public class DidiV5ConstructJob extends AbstractSimpleElasticJob {

    @Resource
    private DidiConstructDataService didiConstructDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        long start = System.currentTimeMillis();
        try {
            didiConstructDataService.process();
        } catch (Exception e) {
            String title = "滴滴上报构造任务，单次运行异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage(), title));
        }
        log.warn("滴滴上报构造任务，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
