package com.br.marketing.check.job.dataclean;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
/**
 * @author zhen.Li1
 * @Classname CustomUploadDataCleanJob
 * @Description 定制上传数据清洗JOB
 * @Date 2025/05/06
 */
public class CustomUploadDataCleanJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext context) {




    }
}
