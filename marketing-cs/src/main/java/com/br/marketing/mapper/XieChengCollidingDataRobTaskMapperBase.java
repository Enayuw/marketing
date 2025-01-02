package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataRobTask;
import com.br.marketing.entity.XieChengCollidingDataRobTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengCollidingDataRobTaskMapperBase {
    int countByExample(XieChengCollidingDataRobTaskExample example);

    int deleteByExample(XieChengCollidingDataRobTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataRobTask record);

    int insertSelective(XieChengCollidingDataRobTask record);

    List<XieChengCollidingDataRobTask> selectByExample(XieChengCollidingDataRobTaskExample example);

    XieChengCollidingDataRobTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataRobTask record, @Param("example") XieChengCollidingDataRobTaskExample example);

    int updateByExample(@Param("record") XieChengCollidingDataRobTask record, @Param("example") XieChengCollidingDataRobTaskExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataRobTask record);

    int updateByPrimaryKey(XieChengCollidingDataRobTask record);
}