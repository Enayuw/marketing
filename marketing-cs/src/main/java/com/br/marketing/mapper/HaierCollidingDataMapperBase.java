package com.br.marketing.mapper;

import java.util.List;

import com.br.marketing.entity.HaierCollidingData;
import com.br.marketing.entity.HaierCollidingDataExample;
import org.apache.ibatis.annotations.Param;

public interface HaierCollidingDataMapperBase {
    int countByExample(HaierCollidingDataExample example);

    int deleteByExample(HaierCollidingDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(HaierCollidingData record);

    int insertSelective(HaierCollidingData record);

    List<HaierCollidingData> selectByExampleWithBLOBs(HaierCollidingDataExample example);

    List<HaierCollidingData> selectByExample(HaierCollidingDataExample example);

    HaierCollidingData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") HaierCollidingData record, @Param("example") HaierCollidingDataExample example);

    int updateByExampleWithBLOBs(@Param("record") HaierCollidingData record, @Param("example") HaierCollidingDataExample example);

    int updateByExample(@Param("record") HaierCollidingData record, @Param("example") HaierCollidingDataExample example);

    int updateByPrimaryKeySelective(HaierCollidingData record);

    int updateByPrimaryKeyWithBLOBs(HaierCollidingData record);

    int updateByPrimaryKey(HaierCollidingData record);
}