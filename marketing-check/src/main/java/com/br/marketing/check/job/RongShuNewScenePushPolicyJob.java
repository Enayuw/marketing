package com.br.marketing.check.job;

import com.br.marketing.check.service.RongShuNewScenePushPolicyService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 榕树新场景自动化筛选决策推送（caseAdd + sole）。
 */
@Component
@Slf4j
public class RongShuNewScenePushPolicyJob extends AbstractSimpleElasticJob {

    @Autowired
    private RongShuNewScenePushPolicyService rongShuNewScenePushPolicyService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        rongShuNewScenePushPolicyService.executePushPolicy();
    }
}
