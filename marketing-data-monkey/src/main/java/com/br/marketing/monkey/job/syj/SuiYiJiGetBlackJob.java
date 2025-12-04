package com.br.marketing.monkey.job.syj;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.monkey.service.suiyiji.SuiYiJiBlackService;
import com.br.marketing.monkey.service.suiyiji.SuiYiJiService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @Author zhen.Li1
 * @Date 2025/12/4
 */
@Component
@Slf4j
public class SuiYiJiGetBlackJob extends AbstractSimpleElasticJob {


    @Resource
    private SuiYiJiBlackService suiYiJiBlackService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        String apiCode;
        String jobParameter = shardingContext.getJobParameter();
        if (StringUtils.isNotBlank(jobParameter)) {
            apiCode = jobParameter;
        } else {
            apiCode = "3710222";
        }

        suiYiJiBlackService.blackPushTransfer(apiCode);

    }
}
