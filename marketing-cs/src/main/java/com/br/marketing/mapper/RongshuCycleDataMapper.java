package com.br.marketing.mapper;

import com.br.marketing.entity.RongshuCycleData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RongshuCycleDataMapper extends RongshuCycleDataMapperBase {


    List<RongshuCycleData> getCycleData(@Param("pushDates")List<String> pushDates,@Param("minId") Long mid);







}
