package com.br.marketing.service.Impl.zhijia;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.zhijia.CityCountyDataDTO;
import com.br.marketing.entity.ZhiJiaCarSeriesInfo;
import com.br.marketing.entity.ZhiJiaClueBackData;
import com.br.marketing.entity.ZhijiaCityConfig;
import com.br.marketing.entity.ZhijiaCountyConfig;

import java.util.List;

public interface ZhiJiaDataProcessService {

    String getToken();



    void getCityAndCounty();

    void getBrandAndseries();

    CityCountyDataDTO matchCityAndCounty(List<ZhijiaCityConfig>cityList, List<ZhijiaCountyConfig>countyList,ZhiJiaClueBackData zhiJiaClueBackInfo);

    /**
     * 根据sftp信息获取车辆品牌以及车系信息
     * @author guangxiu.li
     * @date 2024/7/9 14:15
     * @param zhiJiaClueBackInfo
     * @return java.util.List<ZhiJiaClueBackInfo>
     */
    Result<ZhiJiaCarSeriesInfo> getZhiJiaCarInfo(ZhiJiaClueBackData zhiJiaClueBackInfo);

}
