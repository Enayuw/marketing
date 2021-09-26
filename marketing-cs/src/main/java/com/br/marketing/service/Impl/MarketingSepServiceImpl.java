package com.br.marketing.service.Impl;

import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.MarketingSep;
import com.br.marketing.entity.MarketingSepExample;
import com.br.marketing.mapper.MarketingSepMapper;
import com.br.marketing.service.MarketingSepService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class MarketingSepServiceImpl implements MarketingSepService {

    @Resource
    MarketingSepMapper marketingSepMapper;
    @Override
    public String querySepByApiCode(String apiCode) {
        MarketingSepExample sepExample = new MarketingSepExample();
        sepExample.createCriteria().andApiCodeEqualTo(apiCode);
        List<MarketingSep> sepList = marketingSepMapper.selectByExample(sepExample);
        if(sepList.size()>0){
            return Constants.sepMap.get(sepList.get(0).getSep());
        }
        return Constants.sepMap.get(Integer.valueOf(1));
    }
}
