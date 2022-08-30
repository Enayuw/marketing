package com.br.marketing.origin.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class InitDataImpl implements OriginDataService {

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public List<Object> collect(MqFact mqFact, ProcessHandlerContext context) {
        Long infoId = mqFact.getSourceId();
        MarketingSyncInfo marketingSyncInfo = marketingSyncInfoMapper.selectByPrimaryKey(infoId);
        if(marketingSyncInfo == null){

        }

        return null;
    }

    @Override
    public TransferSource source() {
        return TransferSource.INIT_DATA_SET_PROCESS;
    }
}
