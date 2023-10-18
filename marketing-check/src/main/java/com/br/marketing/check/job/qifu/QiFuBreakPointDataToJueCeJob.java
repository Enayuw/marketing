package com.br.marketing.check.job.qifu;

import com.br.marketing.service.QiFuBreakPointDataToJueCeService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @Description 奇富断点自动化数据推决策 3710053→3710105（营销→决策）
 * https://c.100credit.cn/pages/viewpage.action?pageId=130945548
 * @Author hong.chen
 * @CreateTime 2023/10/09
 */
@Component
@Slf4j
public class QiFuBreakPointDataToJueCeJob extends AbstractSimpleElasticJob {
    @Resource
    private QiFuBreakPointDataToJueCeService service;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        service.doProcess(context.getJobParameter());
    }
}
