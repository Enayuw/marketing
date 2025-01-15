package com.br.marketing.service.carclue.match.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.enums.carclue.CarClueDataStatusEnum;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.clueenums.ChannelConfigTypeEnum;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.service.carclue.match.ClueChannelMatchService;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ClueChannelMatchServiceImpl implements ClueChannelMatchService {
    @Resource
    CarClueInfoMapper carClueInfoMapper;

    @Resource
    ClueChannelConfigService clueChannelConfigService;


    @Override
    public void match(CarClueInfo carClueInfo) {
        List<AbstractClueChannelMatch> channelMatchs = clueChannelConfigService.getChannelMatch();
        for (AbstractClueChannelMatch channelMatch : channelMatchs) {
            Result action = channelMatch.action(carClueInfo);
            if (action.isSuccess()) {
                String channelApiCode = clueChannelConfigService.getChannelApiCode(channelMatch.label(), ChannelConfigTypeEnum.MATCH_CONFIG.getValue());
            }
        }
    }
}
