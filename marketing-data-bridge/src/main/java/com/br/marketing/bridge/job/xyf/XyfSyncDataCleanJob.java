package com.br.marketing.bridge.job.xyf;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.XyfSubmitRecord;
import com.br.marketing.enums.XyfSyncStatusEnum;
import com.br.marketing.service.xyf.XyfSyncDataCleanService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 信用飞外呼数据上传清洗定时任务
 *
 * @Description 解析 contactList 入库明细表，并调用上传清洗（通过 XyfSyncDataCleanService）
 * @Author system
 * @CreateTime 2025
 */
@Component
@Slf4j
public class XyfSyncDataCleanJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "【信用飞-上传数据清洗任务】";

    @Resource
    private XyfSyncDataCleanService xyfSyncDataCleanService;

    /**
     * 定时任务入口：调度开始/结束打日志，异常打告警
     */
    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            log.warn(TITLE + "调度开始");
            action();
            log.warn(TITLE + "调度结束");
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.XYF_SERVICEERROR.getCode(), e.getMessage(), TITLE), e);
        }
    }

    /**
     * 查询未上传 record，按批次调用 service 处理；单批异常时标记该批失败并继续下一批
     */
    private void action() {
        List<XyfSubmitRecord> recordList = xyfSyncDataCleanService.listWaitRecords();
        if (CollectionUtils.isEmpty(recordList)) {
            return;
        }
        for (XyfSubmitRecord record : recordList) {
            try {
                xyfSyncDataCleanService.processRecord(record);
            } catch (Exception e) {
                log.warn(TITLE + "处理批次失败 batchId={}, e={}", record.getBatchId(), e.getMessage(), e);
                xyfSyncDataCleanService.updateRecordSyncStatus(record.getId(), XyfSyncStatusEnum.SYNC_FAIL.getCode());
            }
        }
    }
}
