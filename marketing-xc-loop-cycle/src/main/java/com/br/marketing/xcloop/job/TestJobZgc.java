package com.br.marketing.xcloop.job;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * -----------------------------
 * PackageName： com.br.marketing.xcloop.job
 * ClassName：TestJobZgc
 * Description：
 *
 * @author：it-yml CreateTime：2025-11-20
 * -----------------------------
 */
@Component
@Slf4j
public class TestJobZgc extends AbstractSimpleElasticJob {
    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        System.out.println("测试job启动");
    }
}
