package com.br.marketing.mapper;

import com.br.marketing.entity.YiqianbaoData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface YiqianbaoDataMapper extends YiqianbaoDataMapperBase{

    List<YiqianbaoData> getPushData(@Param("localId") Long localId, @Param("dataId")  Long dataId);

}
