package com.br.marketing.monkey.job.zhijia;

import com.br.marketing.service.Impl.zhijia.ZhiJiaClueFeedBackService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * @ClassName ZhiJiaClueFeedBackJob
 * @Description 之家线索回传
 * @Author kongbx
 * @Date 2024/7/8 20:02
 */
@Component
@Slf4j
public class ZhiJiaClueFeedBackJob extends AbstractSimpleElasticJob {

    private final static String TITLE = "【之家创建线索】";

    @Autowired
    ZhiJiaClueFeedBackService service;


    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        try {
            long start = System.currentTimeMillis();
            service.process();
            long end = System.currentTimeMillis();
            log.warn(TITLE + "调度结束, 耗时:{}", end - start);
        } catch (Exception e) {
            log.error(TITLE + "推送异常", e);
        }
    }
}
