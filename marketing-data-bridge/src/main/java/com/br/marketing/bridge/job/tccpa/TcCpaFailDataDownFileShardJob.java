package com.br.marketing.bridge.job.tccpa;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.tccpa.TcCpaFailDownFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Descrpiotn 同程易融CPA-撞库失败拉取文件/文件信息入库
 * @Author zhiyong.zhang
 *  @CreateTime 2025/08/11
 */

@Component
@Slf4j
public class TcCpaFailDataDownFileShardJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融CPA:FAIL-downFileShard任务】";


    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcCpaFailDownFileService tcCpaFailDownFileService;


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            tcCpaFailDownFileService.process(marketingCommonConfig.getTcyrCpaApiCode());
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_CPA_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }
}
