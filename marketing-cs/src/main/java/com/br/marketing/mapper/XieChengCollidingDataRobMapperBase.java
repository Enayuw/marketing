package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataRob;
import com.br.marketing.entity.XieChengCollidingDataRobExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataRobMapperBase {
    int countByExample(XieChengCollidingDataRobExample example);

    int deleteByExample(XieChengCollidingDataRobExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataRob record);

    int insertSelective(XieChengCollidingDataRob record);

    List<XieChengCollidingDataRob> selectByExampleWithBLOBs(XieChengCollidingDataRobExample example);

    List<XieChengCollidingDataRob> selectByExample(XieChengCollidingDataRobExample example);

    XieChengCollidingDataRob selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataRob record, @Param("example") XieChengCollidingDataRobExample example);

    int updateByExampleWithBLOBs(@Param("record") XieChengCollidingDataRob record, @Param("example") XieChengCollidingDataRobExample example);

    int updateByExample(@Param("record") XieChengCollidingDataRob record, @Param("example") XieChengCollidingDataRobExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataRob record);

    int updateByPrimaryKeyWithBLOBs(XieChengCollidingDataRob record);

    int updateByPrimaryKey(XieChengCollidingDataRob record);
}