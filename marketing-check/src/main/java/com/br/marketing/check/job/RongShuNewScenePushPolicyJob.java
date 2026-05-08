package com.br.marketing.check.job;

import com.br.common.log.AlertLog;
import com.br.marketing.check.service.RongShuNewScenePushPolicyService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 榕树新场景自动化筛选决策推送
 */
@Component
@Slf4j
public class RongShuNewScenePushPolicyJob extends AbstractSimpleElasticJob {

    @Autowired
    private RongShuNewScenePushPolicyService rongShuNewScenePushPolicyService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        try {
            rongShuNewScenePushPolicyService.executePushPolicy();
        } catch (Exception e) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_DECISIONERROR.getCode(),
                            "榕树新场景推决策执行异常" + " " + e.getMessage()),
                    e);
        }
        log.warn("榕树新场景自动化推送决策 execute time:{}", System.currentTimeMillis() - start);
    }
}
