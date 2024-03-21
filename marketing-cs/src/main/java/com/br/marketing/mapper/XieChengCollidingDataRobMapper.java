package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataRobExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface XieChengCollidingDataRobMapper extends XieChengCollidingDataRobMapperBase{

    List<XieChengCollidingDataRob> getRobCollidingDataList(@Param("limit") Integer limit);
}