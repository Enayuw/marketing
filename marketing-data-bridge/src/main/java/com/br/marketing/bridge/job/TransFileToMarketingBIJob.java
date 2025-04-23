package com.br.marketing.bridge.job;

import com.br.marketing.service.TransFileToMarketingBiService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author peng.kang
 * @description: 内部服务器的转化文件落库到marketingBI
 * @date 2025/4/22 16:04
 */
@Component
@Slf4j
public class TransFileToMarketingBIJob extends AbstractSimpleElasticJob {
    @Resource
    TransFileToMarketingBiService transFileToMarketingBiService;

    /**
    * @description: context 参数格式 yyyyMMdd,yyyyMMdd...
    */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long curTime = System.currentTimeMillis();
        log.warn("内部服务器转化文件提取到marketingBI任务开始");
        transFileToMarketingBiService.transFileToMarketingBiProcess(context.getJobParameter());
        log.warn("内部服务器转化文件提取到marketingBI任务结束, 耗时:{}s", (System.currentTimeMillis() - curTime) / 1000);
    }
}
