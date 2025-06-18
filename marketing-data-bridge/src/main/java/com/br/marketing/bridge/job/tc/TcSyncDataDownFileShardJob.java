package com.br.marketing.bridge.job.tc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.MarketingTcyrSyncRecord;
import com.br.marketing.enums.TcSyncRecordStatusEnum;
import com.br.marketing.mapper.MarketingTcyrSyncRecordMapper;
import com.br.marketing.service.tc.TcSyncDataDownService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.List;

/**
 * @Description 同城易融拉取文件,1、txt信息数据入库 2、最后统计txt成功入库的条数(b_marketing_tcyr_sync_file)
 * Tc***ShardJob 同程优化速率新增的job
 *
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
    private TcSyncDataDownService downService;

    @Resource
    private MarketingTcyrSyncRecordMapper tcyrSyncRecordMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        log.warn("TITLE:{} 开始执行",TITLE);
        try {
            List<MarketingTcyrSyncRecord> syncRecordList =tcyrSyncRecordMapper.searchTcyrSyncList(
                            marketingCommonConfig.getTcyrApiCode(),
                            TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue());
            syncRecordList.forEach(syncRecord -> downService.dealTcyrTxtFileSync(syncRecord));
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
    }

}
