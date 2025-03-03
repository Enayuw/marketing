package com.br.marketing.check.job.mark;

import com.br.marketing.service.mark.DataWriteBackFileMarkService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @ClassName DataWriteBackFileMarkJob
 * @Description pp停车文件数据回写跑分文件与Doris
 * @Author kongbx
 * @Date 2025/2/19 15:05
 */
@Component
@Slf4j
public class DataWriteBackFileMarkJob extends AbstractSimpleElasticJob {

    @Resource
    private DataWriteBackFileMarkService dataWriteBackFileMarkService;
    private static final String TITLE = "【pp停车文件数据回写】";

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        log.warn(TITLE + "开始运行");
        long start = System.currentTimeMillis();
        dataWriteBackFileMarkService.process(shardingContext.getJobParameter());
        log.warn(TITLE + "运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }

}