package com.br.marketing.check.job.mark;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.IPPDTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

/**
 * pp停车-榕树数据打标规则-高风险打标
 * 推送的数据为转化数据，数据源为推送电销的记录表
 * @author hedongshuo
 * @dateTime 2025-02-18 20:37
 */
@Component
@Slf4j
public class PPDataHighRiskMarkJob extends AbstractSimpleElasticJob {

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {

    }
}
