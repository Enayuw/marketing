package com.br.marketing.service.carclue.match;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.CarClueProvincesInformation;
import com.br.marketing.entity.CarClueRelationalMapping;
import com.br.marketing.entity.CarClueSeriesInformation;
import com.br.marketing.service.carclue.match.config.ClueChannelConfig;

import java.util.List;


public abstract class AbstractClueChannelMatch {


    /**
     * 匹配渠道商规则
     * code 1-命中；0-为命中；
     * 如果命中 需要把命中
     *
     * @param carClueInfo
     * @return
     */
    public abstract Result action(CarClueInfo carClueInfo, List<CarClueProvincesInformation> provincesInfoConfig, List<CarClueSeriesInformation>
            seriesInfoConfig, List<CarClueRelationalMapping> relationalMappingConfig);




    /**
     * 过滤规则的名称
     *
     * @return
     */
    public abstract String label();

}
