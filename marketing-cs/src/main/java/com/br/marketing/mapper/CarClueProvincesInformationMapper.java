package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueProvincesInformation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueProvincesInformationMapper  extends CarClueProvincesInformationMapperBase {


    String getMaxCleanDate();

    int batchInsert(@Param("list") List<CarClueProvincesInformation> list);
}
