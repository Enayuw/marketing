package com.br.marketing.check.job.monitor;

import com.br.cloud.counter.BrCounter;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.monitor.PrometheusMonitorUtils;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * Prometheus监控实时获取ApiCode
 *
 * @author zhen.Li1
 * @dateTime 2024-04-15 15:35
 */
@Component
@Slf4j
public class PrometheusGetApiCode extends AbstractSimpleElasticJob {

    @Resource
    private MarketingCustomerMapper marketingCustomerMapper;


    @Override
    public void process(JobExecutionMultipleShardingContext context) {

        MarketingCustomerExample example = new MarketingCustomerExample();
        example.createCriteria().andStatusEqualTo((byte) 1).andApiCodeNotLike("74%");

        List<MarketingCustomer> list = marketingCustomerMapper.selectByExample(example);

        list.forEach((MarketingCustomer marketingCustomer) -> {
            BrCounter.count(PrometheusMonitorUtils.CUSTOMER_APICODE_METRIC_NAME, marketingCustomer.getApiCode(), marketingCustomer.getName());
        });

    }
}
