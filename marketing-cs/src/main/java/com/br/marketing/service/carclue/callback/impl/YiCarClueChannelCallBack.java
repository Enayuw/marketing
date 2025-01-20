package com.br.marketing.service.carclue.callback.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import org.springframework.stereotype.Service;

/**
 * @ClassName YiCarClueChannelCallBack
 * @Description 易车回调
 * @Author kongbx
 * @Date 2025/1/19 15:13
 */
@Service
public class YiCarClueChannelCallBack extends AbstractClueChannelCallBack {
    @Override
    public Result callback(CarClueInfo carClueInfo) {
        return null;
    }

    @Override
    public String label() {
        return "Yi_Car_Channel_Match";
    }
}
