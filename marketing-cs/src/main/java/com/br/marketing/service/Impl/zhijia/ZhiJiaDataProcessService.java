package com.br.marketing.service.Impl.zhijia;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.zhijia.CityCountyDataDTO;
import com.br.marketing.dto.zhijia.ZhiJiaCarInfoDTO;
import com.br.marketing.entity.*;

import java.util.List;

public interface ZhiJiaDataProcessService {

    String getToken();



    void getCityAndCounty();

    void getBrandAndseries();

    CityCountyDataDTO matchCityAndCounty(List<ZhijiaCityConfig>cityList, List<ZhijiaCountyConfig>countyList,ZhiJiaClueBackData zhiJiaClueBackInfo);


    List<ZhiJiaCarBrandInfo> getCarBrandInfos();


    /**
     * 根据sftp信息获取车辆品牌
     * @param zhiJiaClueBackInfo
     * @param zhiJiaCarBrandInfos
     * @return
     */
    ZhiJiaCarInfoDTO getZhiJiaCarBrandInfo(ZhiJiaClueBackData zhiJiaClueBackInfo, List<ZhiJiaCarBrandInfo> zhiJiaCarBrandInfos);

    List<ZhiJiaCarSeriesInfo> getCarSeriesInfos(int brandId);


    /**
     * 根据sftp信息获取车系信息
     * @param zhiJiaClueBackInfo
     * @param zhiJiaCarSeriesInfos
     * @return
     */
    ZhiJiaCarInfoDTO getZhiJiaCarSeriesInfo(ZhiJiaClueBackData zhiJiaClueBackInfo, List<ZhiJiaCarSeriesInfo> zhiJiaCarSeriesInfos);

    List<ZhijiaCityConfig> getCityConfigList();

    List<ZhijiaCountyConfig> getCountyConfigList();

}
