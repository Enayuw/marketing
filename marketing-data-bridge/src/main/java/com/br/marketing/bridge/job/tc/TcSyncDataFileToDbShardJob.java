package com.br.marketing.bridge.job.tc;


import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.tc.TcSyncDataFileToDbService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 同城易融fileToDb,原始数据入库(b_marketing_tcyr_sync)
 * Tc***ShardJob 同程优化速率新增的job
 * @Author zhiyong.zhang
 * @CreateTime 2025/06/13
 */

@Component
@Slf4j
public class TcSyncDataFileToDbShardJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【同程易融-fileToDbShard任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataFileToDbService tcSyncDataFileToDbService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        Long start = System.currentTimeMillis();
        String apiCode = marketingCommonConfig.getTcyrApiCode();
        List<Integer> shardingItems = shardingContext.getShardingItems();
        log.warn("TITLE:{}调度开始,apiCode:{},分片:{}",TITLE,apiCode,shardingItems);
        try {
            tcSyncDataFileToDbService.process(apiCode,shardingItems);
        }catch (Exception e){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
        Long end = System.currentTimeMillis();
        log.warn("TITLE:{}调度结束,apiCode:{},耗时:{},分片:{}",TITLE,apiCode,(end - start),shardingItems);
    }
}

