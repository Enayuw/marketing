package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueRelationalMapping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueRelationalMappingMapper extends CarClueRelationalMappingMapperBase{

    String getMaxCleanDate();


    int batchInsert(@Param("list") List<CarClueRelationalMapping> list);
}
