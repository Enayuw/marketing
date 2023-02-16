package com.br.marketing.check.job;

import com.br.marketing.check.service.RongShuIbuCycleService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * @Author lizhen
 * @Date 2023/02/15 20:46
 * @Description:榕树周期性推送人工 IBU 任务
 **/

@Component
@Slf4j
public class RongShuCyclePushDaasJob extends AbstractSimpleElasticJob {


    @Autowired
    private RongShuIbuCycleService rongShuIbuCycleService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        rongShuIbuCycleService.pushCycleDataToDaas();

    }
}
