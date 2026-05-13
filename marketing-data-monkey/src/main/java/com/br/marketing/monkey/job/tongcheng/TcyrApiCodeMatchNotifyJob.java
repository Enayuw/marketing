package com.br.marketing.monkey.job.tongcheng;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.monkey.service.tongcheng.TcyrApiCodeMatchNotifyService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 定时调用灵霄宝殿 {@code tcapiCodeAssign}，对 {@code b_marketing_tcyr_sync_record} 中
 * 已接入成功且尚未分配 apiCode、且 {@code assign_status} 为 NULL（未发起过选码卡片）的批次发起 Agent 匹配；
 * 同一条记录通过 CAS 置 {@code assign_status=1} 保证只发一次卡片，灵霄调用失败会回滚为 NULL 以便重试。
 */
@Component
@Slf4j
public class TcyrApiCodeMatchNotifyJob extends AbstractSimpleElasticJob {

    private static final String TITLE = "【monkey】TcyrApiCodeMatchNotifyJob";

    @Resource
    private TcyrApiCodeMatchNotifyService tcyrApiCodeMatchNotifyService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{} 开始", TITLE);
        try {
            tcyrApiCodeMatchNotifyService.dispatchPendingBatches();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TONGCHENG_SERVICEERROR.getCode(),
                    e.getMessage(), TITLE), e);
        }
        log.warn("{} 结束", TITLE);
    }
}
