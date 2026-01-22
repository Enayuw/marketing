package com.br.marketing.mapper;

import com.br.marketing.entity.DiDiCollidingDataRob;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DiDiV5CollidingDataRobMapper extends DiDiV5CollidingDataRobMapperBase {

    void insertToRobAndUpdateFront(@Param("list") List<DiDiCollidingDataRob> diDiV5CollidingData);

}