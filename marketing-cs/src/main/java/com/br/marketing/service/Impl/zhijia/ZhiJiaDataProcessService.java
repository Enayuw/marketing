package com.br.marketing.service.Impl.zhijia;

import com.br.marketing.dto.zhijia.CityCountyDataDTO;
import com.br.marketing.entity.ZhijiaCityConfig;
import com.br.marketing.entity.ZhijiaCountyConfig;

import java.util.List;

public interface ZhiJiaDataProcessService {


    void getCityAndCounty();

    void getBrandAndseries();

    CityCountyDataDTO matchCityAndCounty(List<ZhijiaCityConfig>cityList, List<ZhijiaCountyConfig>countyList,String city,String county);



}
