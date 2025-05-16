package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueRelationalMapping;
import com.br.marketing.vo.CarClueChannelVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueRelationalMappingMapper extends CarClueRelationalMappingMapperBase{

    String getMaxCleanDate();

    int batchInsert(@Param("list") List<CarClueRelationalMapping> list);

    void insertSql(@Param("querySql") String querySql);

    List<CarClueChannelVo> selectList(@Param("search") String search, @Param("cluePushChannel") String cluePushChannel, @Param("maxDate") String maxDate);

    String getMaxCleanDateByApiCode(@Param("cluePushChannel") String cluePushChannel);

}
