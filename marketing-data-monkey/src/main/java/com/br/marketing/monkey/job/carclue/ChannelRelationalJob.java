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
    private static final String TITLE = "【车线索外采数据相关-】";

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        log.warn("{}开始执行", TITLE);
        long startTime = System.currentTimeMillis();

        // 获取省市/车辆 字典信息
        channelRelationalService.getProvinceAndCity();

        // 处理当天的 易车KA 外采初始配置
        channelRelationalService.getInitMapping();

        // 处理待清洗文档 外采初始配置
        channelRelationalService.getFileInitMapping();

        log.warn("{}执行完成, 耗时{}ms", TITLE, System.currentTimeMillis() - startTime);
    }

}
