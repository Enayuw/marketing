package com.br.marketing.monkey.job;

import com.br.marketing.monkeydata.entity.commonobj.MonkeyContext;
import com.br.marketing.monkeydata.entity.commonobj.PageCondition;
import com.br.marketing.monkeydata.handle.IMonkeyDataHandle;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import org.springframework.beans.factory.annotation.Autowired;

public class TestJob extends AbstractSimpleElasticJob {

    @Autowired
    IMonkeyDataHandle zhongAnHandleImpl;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        PageCondition pageCondition = new PageCondition();
        pageCondition.setPageIndex(1);
        MonkeyContext.setProcessContext("123");
        zhongAnHandleImpl.action(pageCondition);
    }
}
