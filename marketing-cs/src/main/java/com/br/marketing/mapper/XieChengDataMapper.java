package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengData;
import com.br.marketing.entity.XieChengDataExample;
import com.br.marketing.entity.YiqianbaoData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengDataMapper extends XieChengDataMapperBase{



    List<XieChengData> selectByLocalId(@Param("localId") Long localId);

}