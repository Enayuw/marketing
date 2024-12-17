package com.br.marketing.check.job.zhongan;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.bi.ReportStatisticService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


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
            if (ObjectUtil.isNotEmpty(context)){
                String jobParameter = context.getJobParameter();
                LocalDateTime myParam = isMyParam(jobParameter);
                reportStatisticService.action(myParam, null);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.ZHONGAN_REPORTEERROR.getCode(), "众安报表定时统计发生错误！"), e);
        }
    }

    public LocalDateTime isMyParam(String jobParameter) {
        if (ObjectUtil.isNotEmpty(jobParameter)) {
            String string = "23:59:59.999";
            String dateNow = jobParameter + " " + string;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            LocalDateTime localDateTime = LocalDateTime.parse(dateNow, formatter);
            return localDateTime;
        }
        return LocalDateTime.now();
    }

}
