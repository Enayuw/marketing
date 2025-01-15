package com.br.marketing.monkey.job.carclue;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @ClassName CarClueCallbackJob
 * @Description 车线索回调
 * @Author kongbx
 * @Date 2025/1/15 15:18
 */
@Component
@Slf4j
public class CarClueCallbackJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext context) {

    }
}
