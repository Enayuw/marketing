package com.br.marketing.mapper;

import com.br.marketing.entity.HaierCollidingData;
import com.br.marketing.entity.HaierCollidingDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierCollidingDataMapperBase {
    int countByExample(HaierCollidingDataExample example);

    int deleteByExample(HaierCollidingDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(HaierCollidingData record);

    int insertSelective(HaierCollidingData record);

    List<HaierCollidingData> selectByExample(HaierCollidingDataExample example);

    HaierCollidingData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") HaierCollidingData record, @Param("example") HaierCollidingDataExample example);

    int updateByExample(@Param("record") HaierCollidingData record, @Param("example") HaierCollidingDataExample example);

    int updateByPrimaryKeySelective(HaierCollidingData record);

    int updateByPrimaryKey(HaierCollidingData record);
}