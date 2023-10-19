package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingDataValidConfigDefault;
import com.br.marketing.entity.VariableDic;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * Created by Bairong on 2019/8/20.
 */
public interface MarketingValidityChangeMapper {

    /**
     * 有效期记录列表
     * @param isDel
     * @param createTime
     * @param appletDate
     * @param apiCode
     * @param userType
     * @param validStartDate
     * @param validEndDate
     * @param validDays
     * @param validType
     * @param updateTime
     * @param id
     * @return
     */
    @AddDataAuth
    List<MarketingDataValidConfig> selectValidityList(@Param("isDel")Integer isDel, @Param("createTime")String createTime,
                                                      @Param("appletDate")String appletDate, @Param("apiCode")String apiCode,
                                                      @Param("userType")String userType, @Param("validStartDate")String validStartDate,
                                                      @Param("validEndDate")String validEndDate, @Param("validDays")String validDays,
                                                      @Param("validType")Integer validType, @Param("updateTime")String updateTime,
                                                      @Param("id")String id);

    /**
     * 根据ID查找有效期记录
     * @param id
     * @return
     */
    MarketingDataValidConfig selectById(@Param("id") Long id);

    /**
     * 修改有效期记录数据
     * @param config
     * @return
     */
    Integer updateById(@Param("config")MarketingDataValidConfig config);

    /**
     * 新增有效期记录
     * @param config
     * @return
     */
    Integer insertMarketingDataValidConfig(@Param("config")MarketingDataValidConfig config);

    Integer insertValidConfigDefault(@Param("validConfigDefault") MarketingDataValidConfigDefault validConfigDefault);

    Integer selectNum(@Param("apiCode") String apiCode, @Param("userType") String userType);

    Integer selectValidDaysDefault(@Param("apiCode") String apiCode, @Param("userType") String userType);

    Long selectId(@Param("apiCode") String apiCode, @Param("userType") String userType);

    Integer updateMarketingDataValidConfigDefault(@Param("validConfigDefault") MarketingDataValidConfigDefault validConfigDefault);
}
