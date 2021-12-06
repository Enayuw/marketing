package com.br.marketing.mapper;

import com.br.marketing.entity.HaierData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierDataMapper extends HaierDataMapperBase {

    List<HaierData> selectDataLimitId(@Param("localId") Long localId, @Param("minId") Long minId);

    Long selectMinId(@Param("localId") Long localId);
}