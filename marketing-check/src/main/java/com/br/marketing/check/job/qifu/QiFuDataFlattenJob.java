package com.br.marketing.check.job.qifu;

import com.br.marketing.check.service.qifu.QiFuDataFlattenService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 奇富定制前置表数据打平Job
 * 将 b_drs_customize_upload_data_original 表的数据打平到 b_qifu_upload_data_original 表
 */
@Component
@Slf4j
public class QiFuDataFlattenJob extends AbstractSimpleElasticJob {
    @Resource
    private QiFuDataFlattenService qiFuDataFlattenService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("奇富定制前置表数据打平开始");
        long start = System.currentTimeMillis();
        qiFuDataFlattenService.flattenDataProcess();
        long end = System.currentTimeMillis();
        log.warn("奇富定制前置表数据打平耗时：" + (end - start) + "ms");
    }
}

