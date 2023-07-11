package com.br.marketing.check.job;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 描述：： 携程新版短信撞库 job
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName XieChengSmsCollidingDataVtwoToSendJob
 * @author: it-yml
 * @create: 2023-07-11 19:31
 * @Version 1.0
 * --------------------------------------
 **/
@Component
@Slf4j
public class XieChengSmsCollidingDataVtwoToSendJob extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

    }
}
