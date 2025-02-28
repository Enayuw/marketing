package com.br.marketing.check.job.mark;

import com.br.marketing.service.mark.DataCellDecodeMarkService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 白名单打标
 * @author guangchao.zhang
 * @dateTime 2025-02-18 20:37
 */
@Component
@Slf4j
public class DataCellDecodeMarkJob extends AbstractSimpleElasticJob {


    @Autowired
    private DataCellDecodeMarkService dataCellDecodeMarkService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        long start = System.currentTimeMillis();
        dataCellDecodeMarkService.process();
        log.warn("pp停车cell加解密数据，运行耗时：{}s", (System.currentTimeMillis() - start) / 1000);
    }
}
