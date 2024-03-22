package com.br.marketing.xcloop.job;

import com.br.marketing.service.Impl.xc.XcExceptionDataRetryService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 携程异常重试作业
 * @Author hong.chen
 * @CreateTime 2024/03/20
 */
@Component
@Slf4j
public class XcExceptionDataRetryJob extends AbstractSimpleElasticJob {
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    XcExceptionDataRetryService service;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        // 判断强制开启撞库开关
        if (marketingCommonConfig.getXieChengForceOpenSwitch()) {
            // 执行重试撞库
            process();
        } else {
            // 判断是否需要打开条件开关、发送钉钉告警、重试撞库
            // 查询堆积量级是否超限（10w）
            // 查log表是否存在：create_time=当天且business_code=707
            // 查TRUE表release_time=7天后的量级是否超限（500w）

            // 查询条件开启撞库开关（redis）、日志打印开关状态
            // 如开关是开启状态：1.需要关闭，则关闭后发送钉钉告警,return。2.执行重试撞库
            // 如开关是关闭状态：1.需要开启，则开启后执行重试撞库。2.do-nothing
            service.conditonProcess();
        }
    }

    // 执行重试撞库
    private void process() {
        service.process();
    }
}
