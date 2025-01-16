package com.br.marketing.service.Impl.carclue;

import com.br.marketing.entity.CarChannelConfig;
import com.br.marketing.entity.CarClueInfo;

import java.util.List;

public interface CarClueService {
    void carClueCleanHandler(CarClueInfo carClueInfo, Object brandCitycConfig, List<CarChannelConfig> channelConfigList);
}
