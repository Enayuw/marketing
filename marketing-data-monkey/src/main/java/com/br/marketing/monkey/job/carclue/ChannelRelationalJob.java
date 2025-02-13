package com.br.marketing.monkey.job.carclue;

import com.br.marketing.service.carclue.ChannelRelationalService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName ChannelRelationalJob
 * @Description 外采渠道商信息维护
 * @Author kongbx
 * @Date 2025/1/19 17:04
 */
@Component
@Slf4j
public class ChannelRelationalJob extends AbstractSimpleElasticJob {
    @Resource
    private ChannelRelationalService channelRelationalService;
    private static final String TITLE = "【外采渠道商信息维护】";

    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        log.warn(TITLE + "start");
        long start = System.currentTimeMillis();
        //获取省市/车辆信息
        channelRelationalService.getProvinceAndCity();
        //维护外采渠道商信息
        channelRelationalService.relationalMapping();
        long end = System.currentTimeMillis();
        log.warn(TITLE + "end, 耗时{}ms", end-start);

    }
}
