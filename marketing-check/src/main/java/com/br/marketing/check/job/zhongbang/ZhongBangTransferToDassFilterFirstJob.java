package com.br.marketing.check.job.zhongbang;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.ZhongBangToDassFilterProcessService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


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
            // requestDate可配置，param样例：2023-08-08
            String parameter = context.getJobParameter();
            LocalDate requestDate = LocalDate.now();
            if (StringUtils.isNotBlank(parameter)) {
                requestDate = LocalDate.parse(parameter, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            zhongBangToDassFilterProcessService.doProcessFirst(requestDate);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

