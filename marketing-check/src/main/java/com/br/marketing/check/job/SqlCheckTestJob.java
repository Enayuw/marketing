package com.br.marketing.check.job;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.service.IYiXinTransferService;
import com.br.marketing.service.Impl.MarketingCustomertestImpl;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;


@Component
@Slf4j
public class SqlCheckTestJob extends AbstractSimpleElasticJob {

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    MarketingCustomertestImpl marketingCustomertest;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {

        List<MarketingCustomer> marketingOftikvs = marketingCustomertest.getMarketingOftikvs();
        System.out.println(JSON.toJSON(marketingOftikvs));
        List<MarketingCustomer> marketingCustomers1 = marketingCustomerMapper.selectCustomerBytestPartiontikv_();
        System.out.println(JSON.toJSON(marketingCustomers1));
        List<MarketingCustomer> marketingCustomers2 = marketingCustomerMapper.selectCustomerBytestPartiontifh_();
        System.out.println(JSON.toJSON(marketingCustomers2));
        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectCustomerBytestPartion();
        System.out.println(JSON.toJSON(marketingCustomers));
    }
}
