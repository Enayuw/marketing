package com.br.marketing.check.job;

import com.br.common.log.AlertLog;
import com.br.marketing.check.service.RongShuNewScenePushBlackListService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 榕树新场景外呼黑名单（blackData）：本 Job 只处理两路——上传 userType=202（当天）；转化 request_data=T-N（N Speed）。
 */
@Component
@Slf4j
public class RongShuNewScenePushBlackListJob extends AbstractSimpleElasticJob {

    @Autowired
    private RongShuNewScenePushBlackListService rongShuNewScenePushBlackListService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        try {
            rongShuNewScenePushBlackListService.executePushBlackList();
        } catch (Exception e) {
            log.warn(
                    AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                            "榕树新场景推送外呼黑名单执行异常" + " " + e.getMessage()),
                    e);
        }
        log.warn("榕树新场景推送外呼黑名单 execute time:{}", System.currentTimeMillis() - start);
    }
}
