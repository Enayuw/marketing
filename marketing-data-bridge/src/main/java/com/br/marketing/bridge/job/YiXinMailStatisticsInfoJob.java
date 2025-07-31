package com.br.marketing.bridge.job;

import com.br.marketing.bridge.service.YiXInMailStatisticsInfoService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author xiong.luo
 * @description: 宜信邮件统计数据入库JOB
 * @date 2025/7/7 20:04
 */
@Component
@Slf4j
public class YiXinMailStatisticsInfoJob extends AbstractSimpleElasticJob {

    @Resource
    private YiXInMailStatisticsInfoService yiXInMailStatisticsInfoService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long curTime = System.currentTimeMillis();
        log.warn("宜信邮件统计信息提取到marketingBI任务开始");
        yiXInMailStatisticsInfoService.transMailToMarketingBiProcess(context.getJobParameter());
        log.warn("宜信邮件统计信息提取到marketingBI任务结束, 耗时:{}s", (System.currentTimeMillis() - curTime) / 1000);
    }
}