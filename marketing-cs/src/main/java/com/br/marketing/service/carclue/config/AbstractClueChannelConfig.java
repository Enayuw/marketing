package com.br.marketing.service.carclue.config;

import com.br.marketing.entity.CarClueInitMapping;
import com.br.marketing.entity.CarClueProvincesInformation;
import com.br.marketing.entity.CarClueRelationalMapping;
import com.br.marketing.entity.CarClueSeriesInformation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public abstract class AbstractClueChannelConfig {
    /**
     * 解析初始配置并清洗至映射表
     */
    public abstract void verifyCarClueInit(StringBuilder stringBuilder, CarClueRelationalMapping carClueRelationalMapping,
                                           CarClueInitMapping carClueInitMapping, Map<String, List<CarClueProvincesInformation>> provinceNameMap,
                                           Map<String, List<CarClueProvincesInformation>> cityNameMap, Map<String, List<CarClueSeriesInformation>> brandNameMap,
                                           Map<String, List<CarClueSeriesInformation>> subBrandNameMap);

    /**
     * 过滤规则的名称
     * @return
     */
    public abstract String label();

}
