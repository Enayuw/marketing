package com.br.marketing.mapper;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.entity.XieChengDataExample;
import com.br.marketing.entity.YiqianbaoData;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface XieChengDataMapper extends XieChengDataMapperBase{



    List<XieChengData> selectByLocalId(@Param("localId") Long localId,@Param("minId") Long minId);

    List<String> selectLocalIdByNotSend();

    List<XieChengData> getByCellToday(@Param("cell") String cell, JSONArray apiCodes);

    List<XieChengData> getByCellTodayAndLocalId(@Param("createDate")Integer createDate, @Param("minlocalId") Long minlocalId);
}