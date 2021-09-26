package com.br.marketing.service.Impl;

import com.br.marketing.entity.MarketingTaskExtend;
import com.br.marketing.entity.MarketingTaskExtendExample;
import com.br.marketing.mapper.MarketingSepMapper;
import com.br.marketing.mapper.MarketingTaskExtendMapper;
import com.br.marketing.service.MarketingTaskExtendService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class MarketingTaskExtendServiceImpl implements MarketingTaskExtendService {

    @Resource
    MarketingTaskExtendMapper marketingTaskExtendMapper;

    @Override
    public MarketingTaskExtend getMarketingTaskExtend(Long taskId) {
        MarketingTaskExtendExample extendExample = new MarketingTaskExtendExample();
        extendExample.createCriteria().andIsDelEqualTo(Integer.valueOf(1)).andTaskIdEqualTo(taskId);
        List<MarketingTaskExtend> extendList = marketingTaskExtendMapper.selectByExample(extendExample);
        if(extendList.size()>0){
            return extendList.get(0);
        }
        return null;
    }
}
