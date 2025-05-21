package com.br.marketing.monkey.job.wuba;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.Impl.wuba.WuBaQueryDelayCollidingDataZipService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 58查询当天延期撞库数据作业
 * @Author hong.chen
 * @CreateTime 2025/05/08
 */
@Component
@Slf4j
public class WuBaQueryDelayCollidingDataZipJob extends AbstractSimpleElasticJob {
    @Resource
    WuBaQueryDelayCollidingDataZipService service;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        try {
            service.process(shardingContext);
        } catch (Exception e) {
            String title = "58查询当天延期撞库数据作业，单次运行异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_WUBA.getCode(), e.getMessage()
                    , title));
        }
        log.warn("58查询当天延期撞库数据作业，单次运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
