package com.br.marketing.xcloop.job.taikang;

import com.br.marketing.client.taikang.TaikangClient;
import com.br.marketing.client.taikang.TaikangMarketingEvent;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 数据推送泰康
 *
 */
@Component
@Slf4j
public class TaiKangPushDataCustomerJob extends AbstractSimpleElasticJob {

    @Resource
    TaikangClient taikangClient;
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        TaikangMarketingEvent taikangMarketingEvent = new TaikangMarketingEvent();
        taikangMarketingEvent.setApplicantPhone("15100380090");
        taikangMarketingEvent.setBrowseDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        taikangClient.process(taikangMarketingEvent);
    }
}
