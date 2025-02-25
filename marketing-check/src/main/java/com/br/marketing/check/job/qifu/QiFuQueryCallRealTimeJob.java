package com.br.marketing.check.job.qifu;

import com.br.marketing.service.Impl.qifu.qifuai.QiFuAIService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 奇富ai查询外呼信息
 */
@Component
@Slf4j
public class QiFuQueryCallRealTimeJob extends AbstractSimpleElasticJob {


    @Autowired
    private QiFuAIService qiFuAIService;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("奇富AI查询外呼信息开始");
        long start = System.currentTimeMillis();
        qiFuAIService.queryCallMessage();
        log.warn("奇富AI查询外呼信息耗时{} ms", System.currentTimeMillis() - start);


    }
}
