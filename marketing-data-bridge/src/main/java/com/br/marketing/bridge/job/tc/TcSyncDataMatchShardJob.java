package com.br.marketing.bridge.job.tc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.tc.TcSyncDataMatchService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * @Description 同城易融匹配数据 (b_marketing_tcyr_sync_file)
 * Tc***ShardJob 同程优化速率新增的job
 * @Author zhiyong.zhang
 * @CreateTime 2025/06/13
 * 已下线 2025/12/19
 */
@Deprecated
@Component
@Slf4j
public class TcSyncDataMatchShardJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融-matchShard任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataMatchService  tcSyncDataMatchService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        log.warn("TITLE:{} 开始执行",TITLE);
        try {
            tcSyncDataMatchService.shardProcess(marketingCommonConfig.getTcyrApiCode());
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
        Long end = System.currentTimeMillis();
    }
}

