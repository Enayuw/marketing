package com.br.marketing.check.job.zhongbang;


import com.br.marketing.service.ZhongBangToDassFilterProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @Author chenh
 * @Date 2023/8/23 17:16
 * @Description: 众邦转化数据推人工转化过滤接口(首次)
 **/
@Component
@Slf4j
public class ZhongBangTransferToDassFilterFirstJob extends AbstractSimpleElasticJob {
    @Resource
    private ZhongBangToDassFilterProcessService zhongBangToDassFilterProcessService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        try {
            zhongBangToDassFilterProcessService.doProcessFirst();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

