package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueSeriesInformation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueSeriesInformationMapper extends CarClueSeriesInformationMapperBase{


    String getMaxCleanDate();


    int batchInsert(@Param("list") List<CarClueSeriesInformation> list);
}
