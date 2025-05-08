package com.br.marketing.mapper.rulecleaning;


import com.br.marketing.entity.MarketingCustomerOriginalData;
import com.br.marketing.mapper.MarketingCustomerOriginalDataMapperBase;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@Mapper
public interface MarketingCustomerOriginalDataMapper extends MarketingCustomerOriginalDataMapperBase {


    Long getCustomUploadDataNum(@Param("apiCode")String apiCode, @Param("list")List<String> appletDate);

    List<MarketingCustomerOriginalData> getCustomUploadData(@Param("apiCode")String apiCode, @Param("appletDate")String appletDate,
                                                            @Param("indexId")Long indexId);
}