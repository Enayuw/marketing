package com.br.marketing.service.carclue.callback.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import org.springframework.stereotype.Service;

/**
 * @ClassName StarZhiJiaClueChannelCallBack
 * @Description 海星之家回调
 * @Author kongbx
 * @Date 2025/1/19 15:13
 */
@Service
public class StarZhiJiaClueChannelCallBack extends AbstractClueChannelCallBack {
    @Override
    public Result callback(CarClueInfo carClueInfo) {
        return null;
    }

    @Override
    public String label() {
        return "Star_Zhijia_Channel_Match";
    }
}
