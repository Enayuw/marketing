package com.br.marketing.check.job;


import com.br.marketing.service.Impl.XiechengPushImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class XiechengUpdateDataJob extends AbstractSimpleElasticJob {


    @Autowired
    XiechengPushImpl xiechengPush;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        log.warn("开始执行");
        String date = shardingContext.getJobParameter();
        xiechengPush.pushXieCheng(date);
        log.warn("执行结束");
    }
}
