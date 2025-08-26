package com.br.marketing.bridge.job.tccpa;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.tccpa.TcCpaTransferCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 同程易融CPA-转化数据清洗任务
 */
@Component
@Slf4j
public class TcCpaTransferCleanJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融CPA-转化数据清洗任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaTransferCleanService tcCpaTransferCleanService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            tcCpaTransferCleanService.process(marketingCommonConfig.getTcyrCpaApiCode());
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),e.getMessage(), TITLE), e);
        }
    }
}
