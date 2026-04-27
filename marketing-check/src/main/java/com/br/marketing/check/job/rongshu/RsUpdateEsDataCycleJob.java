package com.br.marketing.check.job.rongshu;

import com.br.marketing.check.service.Impl.rongshu.RongShuUpdateEsService;
import com.br.marketing.common.utils.StringUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 榕树更新ES的数据（周期：每周日）
 *
 * @author zhen.Li1
 * @dateTime 2026/04/09 19:30
 */
@Component
@Slf4j
public class RsUpdateEsDataCycleJob extends AbstractSimpleElasticJob {

    @Autowired
    RongShuUpdateEsService rongShuUpdateEsService;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        String jobParameter = context.getJobParameter();
        String apiCode = StringUtils.isNotEmpty(jobParameter) ? jobParameter : "4004643";

        rongShuUpdateEsService.updateESCycleDataByRS(apiCode);

    }


}
