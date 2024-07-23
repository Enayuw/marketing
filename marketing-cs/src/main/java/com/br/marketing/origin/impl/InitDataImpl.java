package com.br.marketing.origin.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncInfo;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InitDataImpl implements OriginDataService {

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public List<Object> collect(MqFact mqFact, ProcessHandlerContext context) {
        Long infoId = mqFact.getSourceId();
        MarketingSyncInfo marketingSyncInfo = marketingSyncInfoMapper.selectByPrimaryKey(infoId);
        if(marketingSyncInfo == null){
            return new ArrayList<>();
        }
        context.setApiCode(marketingSyncInfo.getApiCode());
        context.setTransferInfoId(infoId);
        context.setMqFact(mqFact);
        List<MarketingSyncUser> vaildUserByRequestId = marketingSyncInfoMapper.getVaildUserByRequestId(marketingSyncInfo.getApiCode(), marketingSyncInfo.getRequestBatch());
        return new ArrayList<>(vaildUserByRequestId);
    }

    @Override
    public TransferSource source() {
        return TransferSource.INIT_DATA_SET_PROCESS;
    }

    @Override
    public List<Long> getIdList(List<Object> collect) {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            MarketingSyncUser syncUser = (MarketingSyncUser)collect.get(i);
            idList.add(syncUser.getId());
        }
        return idList;
    }
}
