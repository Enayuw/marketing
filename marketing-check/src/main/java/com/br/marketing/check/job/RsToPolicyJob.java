package com.br.marketing.check.job;

import com.br.marketing.service.RsTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 新版本榕树自动化转决策功能{@link com.br.marketing.check.service.Impl.RongShuPushDecisionServiceImpl}
 * 已经在8月1号上线，所以RsToPolicyJob在后续观察没有问题，就可以删除了
 * @Author yu.xia@brgroup.com
 * @Date 2024/8/7 11:30
 * @deprecated 后续观察没有问题，就可以删除了
 */
@Deprecated
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
