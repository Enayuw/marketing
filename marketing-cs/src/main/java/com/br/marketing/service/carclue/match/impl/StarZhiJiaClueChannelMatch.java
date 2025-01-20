package com.br.marketing.service.carclue.match.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import org.springframework.stereotype.Service;

@Service
public class StarZhiJiaClueChannelMatch extends AbstractClueChannelMatch {


    @Override
    public Result action(CarClueInfo carClueInfo) {
        return null;
    }

    @Override
    public String label() {
        return "Star_Zhijia_Channel_Match";
    }
}
