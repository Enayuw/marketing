package com.br.marketing.check.job;

import com.br.marketing.service.thirdpartner.ThirdPartnerDataBackService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;

/**
 * @description 三方数据有效期变更回传任务
 * @author hedongshuo
 * @date 2024/11/28 20:49
 **/
@Component
@Slf4j
public class ThirdPartnerDataValidPassBackJob extends AbstractSimpleElasticJob {

    @Resource
    ThirdPartnerDataBackService thirdPartnerDataBackService;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        thirdPartnerDataBackService.validChangeDataBack();
    }
}
