package com.br.marketing.check.job.qifu;

import com.br.marketing.service.DataCleanQiFu360Service;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * D20240622促动支用户信息清洗job-3710139(营销)
 * 需求地址：https://c.100credit.cn/pages/viewpage.action?pageId=166635143
 * 技术方案地址：https://c.100credit.cn/pages/viewpage.action?pageId=166636894
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-06-25
 */
@Component
@Slf4j
public class DataCleanQiFu360Job extends AbstractSimpleElasticJob {
    @Resource
    private DataCleanQiFu360Service dataCleanQiFu360Service;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String uuid = UUID.randomUUID().toString();
        String jobParameter = context.getJobParameter();
        log.warn("DataCleanQiFu360Job-start-{}-jobParam:[{}]",uuid,jobParameter);
        dataCleanQiFu360Service.cleaning(jobParameter);
        log.warn("DataCleanQiFu360Job-end-{}", uuid);
    }
}
