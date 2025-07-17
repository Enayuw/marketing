package com.br.marketing.monkey.job.zhongan;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.Impl.zhongan.ZhongAnCollidingDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ZhongAnPushRosterLockingDataNewJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongAnCollidingDataService zhongAnCollidingDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        try {
            zhongAnCollidingDataService.process(context);
        } catch (Exception e) {
            String title = "众安名单上报，单次运行异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(), e.getMessage(), title));
        }
        log.warn("众安名单上报作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
