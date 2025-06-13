package com.br.marketing.bridge.job.tc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.enums.TcSyncRecordStatusEnum;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataDownFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 同城易融拉取文件,txt信息数据入库(b_marketing_tcyr_sync_file)
 * Tc***ShardJob 同程优化速率新增的job
 * @Author zhiyong.zhang
 * @CreateTime 2025/06/12
 */
@Component
@Slf4j
public class TcSyncDataDownFileShardJob extends AbstractSimpleElasticJob{

    private final static String TITLE = "【同程易融-downFileShard任务】";

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TcSyncDataDownFileService downFileService;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        Long start = System.currentTimeMillis();
        List<Integer> shardingItems = shardingContext.getShardingItems();
        String apiCode = marketingCommonConfig.getTcyrApiCode();
        log.warn("{}调度开始,apiCode:{},分片:{}",TITLE,apiCode,shardingItems);
        try {
            atciton(apiCode);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
        Long end = System.currentTimeMillis();
        log.warn("{}调度结束,apiCode:{},耗时:{},分片:{}",TITLE,apiCode,(end - start),shardingItems);
    }

    private void atciton(String apiCode) {
        List<MarketingTcyrSyncRecord> syncRecordList =
                tcyrSyncRecordMapper.searchTcyrSyncList(apiCode, TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue());
        for (MarketingTcyrSyncRecord syncRecord : syncRecordList) {
            tcyrSyncRecordMapper.updateTcyrRecordDownStatus(syncRecord.getBatchNo(), 1);
            Result syncResult =downFileService.dealTcyrTxtFileSync(syncRecord);
            if (syncResult != null  && syncResult.isSuccess()) {
                tcyrSyncRecordMapper.updateTcyrRecordDownStatus(syncRecord.getBatchNo(), 2);
            }
        }
    }
}
