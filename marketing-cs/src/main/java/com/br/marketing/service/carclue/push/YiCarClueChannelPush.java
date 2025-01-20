package com.br.marketing.service.carclue.push;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;

/**
 * @ClassName YiCarClueChannelPush
 * @Description 易车推送车线索
 * @Author kongbx
 * @Date 2025/1/19 15:12
 */
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
