package com.br.marketing.service.carclue.callback;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;

/**
 * @ClassName StarZhiJiaClueChannelCallBack
 * @Description 海星之家回调
 * @Author kongbx
 * @Date 2025/1/19 15:13
 */
public class StarZhiJiaClueChannelCallBack extends AbstractClueChannelCallBack{
    @Override
    public Result callback(CarClueInfo carClueInfo) {
        return null;
    }

    @Override
    public String label() {
        return "Star_Zhijia_Channel_Match";
    }
}
