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

}