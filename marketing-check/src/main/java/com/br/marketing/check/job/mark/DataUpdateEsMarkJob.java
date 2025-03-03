package com.br.marketing.check.job.mark;

import com.br.marketing.service.mark.DataUpdateEsMarkService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName DataUpdateEsMarkJob
 * @Description pp停车文件数据更新es数据信息
 * @Author kongbx
 * @Date 2025/2/19 15:01
 */
@Component
@Slf4j
public class DataUpdateEsMarkJob extends AbstractSimpleElasticJob {
    @Resource
    private DataUpdateEsMarkService dataUpdateEsMarkService;
    private static final String TITLE = "【pp停车数据更新es】";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        dataUpdateEsMarkService.process(shardingContext.getJobParameter());
        log.warn(TITLE + "运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }

}
