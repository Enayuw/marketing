package com.br.marketing.service.carclue;

import com.br.marketing.entity.*;
import com.br.marketing.service.carclue.callback.AbstractClueChannelCallBack;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;

import java.util.List;

public interface CarClueService {
    void pushCarClueHandler(List<CarClueInfo> carClueInfoList, AbstractClueChannelPush channelPushImpl);

    void carClueCallBackHandler(List<CarClueInfo> carClueInfoList, AbstractClueChannelCallBack channelCallBackImpl);

    void carClueCleanHandler(CarClueInfo carClueInfo, List<CarClueProvincesInformation> carClueProvincesInfoList, List<CarClueSeriesInformation>
     carClueSeriesInfoList, List<CarClueRelationalMapping> carClueRelationalMappingList, List<CarChannelConfig> channelConfigList) throws Exception;
}
