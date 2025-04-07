package com.br.marketing.monkey.job.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.Impl.wuba.WuBaCollidingDataSubmitService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 58提交撞库数据作业
 *
 * @Author chenh
 * @Date 2024-07-10
 */
@Component
@Slf4j
public class WuBaCollidingDataSubmitJob extends AbstractSimpleElasticJob {
    @Resource
    WuBaCollidingDataSubmitService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        try {
            service.process(context);
        } catch (Exception e) {
            String title = "58提交撞库名单，单次运行异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , title));
        }
        log.warn("58提交撞库数据作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
