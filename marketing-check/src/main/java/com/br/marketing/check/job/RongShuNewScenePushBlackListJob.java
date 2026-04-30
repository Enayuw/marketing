package com.br.marketing.check.job;

import com.br.marketing.check.service.RongShuNewScenePushBlackListService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 榕树新场景：上传 202、转化 applyResult=1（当天）、转化 request_data=T-N，推送外呼黑名单。
 */
@Component
@Slf4j
public class RongShuNewScenePushBlackListJob extends AbstractSimpleElasticJob {

    @Autowired
    private RongShuNewScenePushBlackListService rongShuNewScenePushBlackListService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        rongShuNewScenePushBlackListService.executePushBlackList();
    }
}
