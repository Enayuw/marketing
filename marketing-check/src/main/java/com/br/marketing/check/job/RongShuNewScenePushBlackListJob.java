package com.br.marketing.check.job;

import com.br.marketing.check.service.RongShuNewScenePushBlackListService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 榕树新场景外呼黑名单（blackData）：本 Job 只处理两路——上传 userType=202（当天）；转化 request_data=T-N（N Speed）。
 * <p>
 * 「T 日转化数据中 applyResult=1 永久拉黑」不在本 Job，由实时/单独链路实现，请勿在此处追加逻辑。
 * </p>
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
