package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfig;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingDataValidConfigMapper extends MarketingDataValidConfigMapperBase{
    @Select("select id,api_code as apiCode,user_type,valid_start_date,valid_end_date,valid_days,valid_type " +
            "from b_marketing_data_valid_config where  api_code=#{apiCode} and  ( (user_type= #{userType} and valid_type = 1) or valid_type = 2) and is_del = 1")
    List<MarketingDataValidConfig> selectInfo(@Param("apiCode")String apiCode,@Param("userType") String userType);
}