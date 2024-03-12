package com.br.marketing.check.job;

import com.br.marketing.service.RsTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RsToPolicyJob  extends AbstractSimpleElasticJob {

    @Autowired
    RsTransferService rsTransferService;

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        String[] split = jobParameter.split(",");
        String apiCode = "";
        String date = "";
        if(split.length>0){
            apiCode = split[0];
        }
        if(split.length>1){
            date = split[1];
        }
        rsTransferService.getRsToPolicy(apiCode,date);
    }
}
