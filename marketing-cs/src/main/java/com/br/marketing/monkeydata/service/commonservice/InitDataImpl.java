package com.br.marketing.monkeydata.service.commonservice;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.monkeydata.entity.InputDataCondition;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.InitByRequestIdDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class InitDataImpl {

    @Autowired
    MarketingSyncUserMapper userMapper;

    @Resource
    MarketingSyncInfoMapper infoMapper;

    public Result<IterationResult> getInputData(InputDataCondition<InitByRequestIdDTO> condition) {
        InitByRequestIdDTO requestIdDTO = condition.getConditon();
        infoMapper.get
        return null;
    }
}
