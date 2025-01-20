package com.br.marketing.service.carclue.push.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;
import org.springframework.stereotype.Service;

/**
 * @ClassName YiCarClueChannelPush
 * @Description 易车推送车线索
 * @Author kongbx
 * @Date 2025/1/19 15:12
 */
@Service
public class YiCarClueChannelPush extends AbstractClueChannelPush {

    @Override
    public Result push(CarClueInfo carClueInfo) {
        return null;
    }

    @Override
    public String label() {
        return "Yi_Car_Channel_Match";
    }

}
