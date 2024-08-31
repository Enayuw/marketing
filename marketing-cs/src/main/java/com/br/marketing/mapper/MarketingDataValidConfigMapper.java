package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mapper
public interface MarketingDataValidConfigMapper extends MarketingDataValidConfigMapperBase {
    /**
     * 查询
     * @param apiCode apiCode
     * @param userType userType
     * @return java.util.List<com.br.marketing.entity.MarketingDataValidConfig> 查询结果
     */
    @Select("select* " +
            "from b_marketing_data_valid_config where  api_code=#{apiCode}  and is_del = 1")
    List<MarketingDataValidConfig> selectInfo(@Param("apiCode") String apiCode, @Param("userType") String userType);

    /**
     * 查询
     * @param apiCode apiCode
     * @param userType userType
     * @return java.util.List<com.br.marketing.entity.MarketingDataValidConfig> 查询结果
     */
    @Select("select* " +
            "from b_marketing_data_valid_config where  api_code=#{apiCode} and user_type =#{userType}  and is_del = 1")
    List<MarketingDataValidConfig> selectInfoFirstVersion(@Param("apiCode") String apiCode, @Param("userType") String userType);

    /**
     * 查询
     * @param apiCode apiCode
     * @return java.util.List<com.br.marketing.entity.MarketingDataValidConfig> 查询结果
     */
    List<MarketingDataValidConfig> findListByApiCodeAndUserType(@Param("apiCode") String apiCode);

    /**
     * 2023-08-01 14:17
     * 分页获取规则
     *
     * @param apiCode     code
     * @param dateStr     日期,格式yyyy-MM-dd
     * @param userTypeSet 场景集合
     * @param page        页号，首页页号为0
     * @param pageSize    页面大小
     * @return 有效期规则集合
     */
    List<MarketingDataValidConfig> findListByApiCodeAndUserTypeSetPagetikv_(@Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr
            , @Param("userTypeSet") Set<String> userTypeSet
            , @Param("page") Integer page
            , @Param("pageSize") Integer pageSize);

    /**
     * 查询 b_marketing_data_valid_config 表中 MIN(valid_start_date), MAX(valid_end_date)
     * @param apiCode apiCode
     * @param dateStr 提取数据的时间
     * @param userTypeSet 场景
     * @return com.br.marketing.entity.MarketingDataValidConfig 查询到的结果对象
     */
    MarketingDataValidConfig queryStartDateEndDatetikv_(@Param("apiCode") String apiCode
            , @Param("dateStr") String dateStr
            , @Param("userTypeSet") Set<String> userTypeSet);


    @Select("select min(valid_start_date) as validStartDate,max(valid_end_date) as validEndDate from b_marketing_data_valid_config " +
            "where api_code = #{apiCode} and is_del = 1 and DATE_FORMAT(NOW(),'%Y-%m-%d') between valid_start_date and valid_end_date")
    MarketingDataValidConfig getMarketingTransferDataWithValidityPeriod(@Param("apiCode") String apiCode);


    /**
     * 根据被修改的有效期id获取该user_type及api_code下全部有效期的最小开始时间及最大结束时间
     *
     * @param id id
     * @return {@link Map }<{@link String },{@link String }>
     * @author senyang.zheng
     * @date 2023/10/09
     */
    Map<String,String> getValidPeriodRangeByApiCodeAndUserType(@Param("id") Long id);

    /**
     * 根据apiCode获取有效期配置
     * @param apiCode apiCode
     * @param appletDate appletDate
     * @return java.util.List<com.br.marketing.entity.MarketingDataValidConfig> 查询结果
     */
    List<MarketingDataValidConfig> getValidityDataByApiCode(@Param("apiCode") String apiCode ,@Param("appletDate") String appletDate);

    /**
     * 根据appletDate获取有效期配置
     * @param apiCode apiCode
     * @param appletDate appletDate
     * @return java.util.List<com.br.marketing.entity.MarketingDataValidConfig> 查询结果
     */
    List<MarketingDataValidConfig> getValidityDataByAppletDate(@Param("apiCode") String apiCode ,@Param("appletDate") String appletDate);

    /**
     * 根据被修改的有效期id获取该user_type及api_code下全部有效期(T-N)的最小开始时间及最大结束时间
     *
     * @param id id
     * @param offsetDay 偏移日
     * @return {@link Map }<{@link String }, {@link String }>
     * @author senyang.zheng
     * @date 2024/01/12
     */
    Map<String, String> getValidPeriodRangeByApiCodeAndUserTypeAndOffsetDay(@Param("id") Long id, @Param("offsetDay") Integer offsetDay);

    void updateBatchById(@Param("ids") List<Long> ids, @Param("validStartDate") String validStartDate, @Param("validEndDate") String validEndDate);

    /**
     * 2024-08-09 15:33
     * 获取有效期内地上传时间
     */
    Set<String> getAppletDateByApiCodeAndDateStr(@Param("apiCode") String apiCode, @Param("dateStr") String dateStr);
}