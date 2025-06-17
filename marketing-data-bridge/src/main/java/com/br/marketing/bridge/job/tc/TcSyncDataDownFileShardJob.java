package com.br.marketing.bridge.job.tc;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
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
        Long start = System.currentTimeMillis();
        String apiCode = marketingCommonConfig.getTcyrApiCode();
        String jobParameter = shardingContext.getJobParameter();
        log.warn("TITLE:{}调度开始,apiCode:{},jobParameter:{}",TITLE,apiCode,jobParameter);
        try {
            if (StringUtils.isEmpty(jobParameter) || jobParameter.equals("dealTcyrTxtFileSync")) {
                dealTcyrTxtFileSync(apiCode);
            }else if (StringUtils.isNotEmpty(jobParameter) && jobParameter.equals("dealTcyrTxtFileCount")) {
                dealTcyrTxtFileCount(apiCode);
            }

        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
        Long end = System.currentTimeMillis();
        log.warn("TITLE:{}调度结束,apiCode:{},耗时:{}",TITLE,apiCode,(end - start));
    }


    /**
     * 处理GZ下载 && syncFile信息入库
     * @param apiCode
     */
    private void dealTcyrTxtFileSync(String apiCode) {
        List<MarketingTcyrSyncRecord> syncRecordList =
                tcyrSyncRecordMapper.searchTcyrSyncList(apiCode, TcSyncRecordStatusEnum.ACCESS_SUCCESS.getValue());
        for (MarketingTcyrSyncRecord syncRecord : syncRecordList) {
            tcyrSyncRecordMapper.updateTcyrRecordDownStatus(syncRecord.getBatchNo(), 1);
            Result syncResult = downService.dealTcyrTxtFileSync(syncRecord);
            if (syncResult != null  && syncResult.isSuccess()) {
                tcyrSyncRecordMapper.updateTcyrRecordDownStatus(syncRecord.getBatchNo(), 2);
            }
        }
    }

    /**
     * 统计syncFile的 dbCount 修改successLine
     * @param apiCode
     */
    private void dealTcyrTxtFileCount(String apiCode) {
        downService.dealTcyrTxtFileCount(apiCode);
    }
}
