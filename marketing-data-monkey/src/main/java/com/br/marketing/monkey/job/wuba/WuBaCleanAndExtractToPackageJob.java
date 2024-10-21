package com.br.marketing.monkey.job.wuba;

import com.br.marketing.service.Impl.wuba.WuBaCleanAndExtractToPackageService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @description 58新客撞库日志清洗及数据提取作业
 * @author hedongshuo
 * @date 2024/10/18 13:45
 **/
@Component
@Slf4j
public class WuBaCleanAndExtractToPackageJob extends AbstractSimpleElasticJob {

    @Resource
    WuBaCleanAndExtractToPackageService wuBaCleanAndExtractToPackageService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        wuBaCleanAndExtractToPackageService.process(context);
        log.warn("58新客撞库日志清洗及数据提取作业，运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
