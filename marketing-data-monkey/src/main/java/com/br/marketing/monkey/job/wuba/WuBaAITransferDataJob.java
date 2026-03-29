package com.br.marketing.monkey.job.wuba;

import com.br.marketing.monkey.service.wuba.WuBaFetchAIDataService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @Description 58AI转化数据拉取
 * @Author xiong.luo
 * @Date 2026-03-17
 */
@Component
@Slf4j
public class WuBaAITransferDataJob extends AbstractSimpleElasticJob {

    @Resource
    private WuBaFetchAIDataService wuBaFetchAIDataService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        wuBaFetchAIDataService.fetchAndProcessData(new Date());
    }
}
