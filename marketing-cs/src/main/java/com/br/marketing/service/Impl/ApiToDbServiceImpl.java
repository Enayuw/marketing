package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.entity.MarketingCustomerExample;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncInfoExample;
import com.br.marketing.mapper.MarketingCustomerMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.IApiToDbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApiToDbServiceImpl  implements IApiToDbService {

    @Autowired
    MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    MarketingSyncInfoMapper syncInfoMapper;

    @Override
    public Result pushToDb() {
//        String format = DateUtils.format(new Date(), "yyyy-MM-dd");
//        MarketingCustomerExample customerExample = new MarketingCustomerExample();
//        customerExample.createCriteria().andStatusEqualTo(new Byte("1"));
//        List<MarketingCustomer> marketingCustomers = marketingCustomerMapper.selectByExample(customerExample);
//        MarketingSyncInfoExample syncInfoExample = new MarketingSyncInfoExample();
//        syncInfoExample.createCriteria().andCreateTimeGreaterThanOrEqualTo().andCreateTimeLessThan(DateUtils.)
//        syncInfoMapper.selectByExample()
//        for (MarketingCustomer marketingCustomer : marketingCustomers) {
//
//        }
        return null;
    }
}
