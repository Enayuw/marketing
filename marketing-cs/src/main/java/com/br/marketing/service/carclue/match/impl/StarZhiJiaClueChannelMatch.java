package com.br.marketing.service.carclue.match.impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.CarClueProvincesInformation;
import com.br.marketing.entity.CarClueRelationalMapping;
import com.br.marketing.entity.CarClueSeriesInformation;
import com.br.marketing.service.carclue.match.AbstractClueChannelMatch;
import com.br.marketing.service.carclue.match.config.StarZhiJiaChannelConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StarZhiJiaClueChannelMatch extends AbstractClueChannelMatch {


    @Override
    public Result action(CarClueInfo carClueInfo, List<CarClueProvincesInformation> provincesInfoConfig, List<CarClueSeriesInformation>
            seriesInfoConfig, List<CarClueRelationalMapping> relationalMappingConfig) {


        return null;
    }

    @Override
    public String label() {
        return "Star_Zhijia_Channel_Match";
    }
}
