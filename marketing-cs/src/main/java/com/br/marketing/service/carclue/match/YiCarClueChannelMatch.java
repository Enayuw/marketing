package com.br.marketing.service.carclue.match;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import org.springframework.stereotype.Service;


@Service
public class YiCarClueChannelMatch extends AbstractClueChannelMatch{


    @Override
    public Result action(CarClueInfo carClueInfo) {
        return null;
    }

    @Override
    public String label() {
        return "Yi_Car_Channel_Match";

    }
}
