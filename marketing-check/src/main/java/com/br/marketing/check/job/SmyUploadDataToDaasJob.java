package com.br.marketing.check.job;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.MarketingSmyPushService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @author guangchao.zhang
 * @Classname SmyUploadDataToDaasJob
 * @Description 萨摩耶数据推daas
 * @Date 2022/11/18 10:02 AM
 */
@Component
@Slf4j
public class SmyUploadDataToDaasJob extends AbstractSimpleElasticJob {


    @Autowired
    private MarketingSmyPushService marketingSmyPushService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        String apiCode = StringUtils.isNotBlank(jobParameter)?jobParameter:"";
        marketingSmyPushService.pushSmyUploadDataToDaas(apiCode);
    }


}
