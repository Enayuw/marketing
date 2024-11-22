package com.br.marketing.check.job.zhongan;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.bi.ReportStatisticService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;


/**
 * 众安经营分析报表每日定时生成任务
 */
@Component
@Slf4j
public class ZhongAnReportStatisticJob extends AbstractSimpleElasticJob {

    @Resource
    ReportStatisticService reportStatisticService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            String actionDate = LocalDate.now().toString();
            reportStatisticService.action(actionDate);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(), "众安报表定时统计发生错误！"), e);
        }
    }

}
