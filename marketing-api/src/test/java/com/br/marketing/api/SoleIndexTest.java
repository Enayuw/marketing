package com.br.marketing.api;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.IRuleConfigService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.vo.CustomerSoleRuleVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MarketingApiApplication.class)
public class SoleIndexTest {

    @Autowired
    SoleStrategyService soleStrategyService;

    @Autowired
    IRuleConfigService iRuleConfigService;

    @Autowired
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Test
    public void soleIndexSql(){
        String apiCode = "7410438";
        Result<List<CustomerSoleRuleVO>> soleConfig = iRuleConfigService.getSoleConfig(apiCode);
        MarketingSyncUser marketingSyncUser = marketingSyncUserMapper.selectMarketingSyncUserById(apiCode, 2002007L);
        Result<Integer> integerResult = soleStrategyService.actionSole(soleConfig.getData(), marketingSyncUser);
        System.out.println(integerResult);
    }
}
