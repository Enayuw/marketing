package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface MarketingDataValidConfigMapper extends MarketingDataValidConfigMapperBase {
    @Select("select* " +
            "from b_marketing_data_valid_config where  api_code=#{apiCode}  and is_del = 1")
    List<MarketingDataValidConfig> selectInfo(@Param("apiCode") String apiCode, @Param("userType") String userType);

    @Select("select* " +
            "from b_marketing_data_valid_config where  api_code=#{apiCode} and user_type =#{userType}  and is_del = 1")
    List<MarketingDataValidConfig> selectInfoFirstVersion(@Param("apiCode") String apiCode, @Param("userType") String userType);

    List<MarketingDataValidConfig> findListByApiCodeAndUserType(@Param("apiCode") String apiCode);

    /**
     * 2023-08-01 14:17
     * 分页获取规则
     *
     * @param apiCode     code
     * @param dateStr     日期,格式yyyy-MM-dd
     * @param userTypeSet 场景集合
     * @param page        页号，首页页号为0
     */
    List<MarketingDataValidConfig> findListByApiCodeAndUserTypeSetPage(@Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr
            , @Param("userTypeSet") Set<String> userTypeSet, int page);
}