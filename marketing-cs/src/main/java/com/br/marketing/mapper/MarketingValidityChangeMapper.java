package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingDataValidConfigDefault;
import org.apache.ibatis.annotations.Param;


/**
 * Created by Bairong on 2019/8/20.
 */
public interface MarketingValidityChangeMapper {


    Integer insertValidConfigDefault(@Param("validConfigDefault") MarketingDataValidConfigDefault validConfigDefault);

    Integer insertSelective(MarketingDataValidConfigDefault validConfigDefault);

    Integer selectNum(@Param("apiCode") String apiCode, @Param("userType") String userType);

    Integer selectValidDaysDefault(@Param("apiCode") String apiCode, @Param("userType") String userType);

    MarketingDataValidConfigDefault selectId(@Param("apiCode") String apiCode, @Param("userType") String userType);

    Integer updateMarketingDataValidConfigDefault(@Param("validConfigDefault") MarketingDataValidConfigDefault validConfigDefault);

    MarketingDataValidConfig getValidDate(@Param("apiCode") String apiCode, @Param("userType") String userType, @Param("appletDate")String appletDate);
}
