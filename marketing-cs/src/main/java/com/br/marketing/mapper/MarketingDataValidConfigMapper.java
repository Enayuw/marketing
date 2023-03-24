package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfig;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDataValidConfigMapper extends MarketingDataValidConfigMapperBase{
    @Select("select* " +
            "from b_marketing_data_valid_config where  api_code=#{apiCode}  and is_del = 1")
    List<MarketingDataValidConfig> selectInfo(@Param("apiCode")String apiCode,@Param("userType") String userType);
}