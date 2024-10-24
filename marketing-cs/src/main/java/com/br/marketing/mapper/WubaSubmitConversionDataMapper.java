package com.br.marketing.mapper;

import com.br.marketing.entity.WubaSubmitConversionData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WubaSubmitConversionDataMapper extends WubaSubmitConversionDataMapperBase{

    List<WubaSubmitConversionData> findByConditionAndPage(
            @Param("apiCode") String apiCode,
            @Param("status") Integer requestDate,
            @Param("pushStatus") Integer pushStatus,
            @Param("createDate") Integer createDate,
            @Param("pageSize") Integer pageSize);

    List<WubaSubmitConversionData> findWithMarketingTimeByPage(
            @Param("apiCode") String apiCode,
            @Param("status") Integer requestDate,
            @Param("pushStatus") Integer pushStatus,
            @Param("userType") String userType,
            @Param("marketingTimeStart") String marketingTimeStart,
            @Param("marketingTimeEnd") String marketingTimeEnd,
            @Param("pageSize") Integer pageSize);

    List<WubaSubmitConversionData> findWithMarketingTimeByIndex(
            @Param("apiCode") String apiCode,
            @Param("marketingTimeStart") String marketingTimeStart,
            @Param("marketingTimeEnd") String marketingTimeEnd,
            @Param("indexId") Long indexId,
            @Param("pageSize") Integer pageSize);

    int batchAdd(List<WubaSubmitConversionData> list);

    List<WubaSubmitConversionData> findSubmitDataByPushTime(
            @Param("apiCode") String apiCode,
            @Param("pushTimeStart") String pushTimeStart,
            @Param("pushTimeEnd") String pushTimeEnd,
            @Param("indexId") Long indexId,
            @Param("pageSize") Integer pageSize
    );

}