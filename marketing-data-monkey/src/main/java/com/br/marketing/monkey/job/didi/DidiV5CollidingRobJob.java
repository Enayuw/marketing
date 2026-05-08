package com.br.marketing.monkey.job.didi;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.service.didi.DiDiCollidingDataRobService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 *
 * 滴滴短信流量数据撞库任务
 * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=230971961">D20251210滴滴短信流量准入接口（sftp→api）-3710223</a>
 *
 * @author senyang.zheng
 * @since 2025/12/17
 */
@Component
@Slf4j
public class DidiV5CollidingRobJob extends AbstractSimpleElasticJob {

    @Resource
    private DiDiCollidingDataRobService diDiCollidingDataRobService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        log.warn("滴滴非周期数据撞库任务开始");
        try {
            diDiCollidingDataRobService.colliding(context);
        } catch (Exception e) {
            String title = "滴滴非周期数据撞库任务，单次运行异常";
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.DIDI_V5_SERVICEERROR.getCode(), e.getMessage(), title));
        }
        log.warn("滴滴非周期数据撞库任务结束，运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}

