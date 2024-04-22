package com.br.marketing.service.Impl;

import com.br.marketing.config.datasourceconfig.datasourceannotion.DbOfDorisMarketing;
import com.br.marketing.config.datasourceconfig.datasourceannotion.DbOfTikvMarketing;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MarketingCustomertestImpl {
    @Resource
    MarketingCustomerMapper marketingCustomerMapper;

    @DbOfTikvMarketing
    @Transactional
    public List<MarketingCustomer> getMarketingOftikvs(){
        return marketingCustomerMapper.selectCustomerBytestPartiontikv_();
    }
}
