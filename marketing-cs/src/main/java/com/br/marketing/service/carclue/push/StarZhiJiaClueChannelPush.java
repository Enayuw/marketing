package com.br.marketing.service.carclue.push;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;

/**
 * @ClassName StarZhiJiaClueChannelPush
 * @Description 海星之家推送车线索
 * @Author kongbx
 * @Date 2025/1/19 15:12
 */
public class StarZhiJiaClueChannelPush extends AbstractClueChannelPush {

    @Override
    public Result push(CarClueInfo carClueInfo) {
        return null;
    }

    @Override
    public String label() {
        return "Star_Zhijia_Channel_Match";
    }

}
