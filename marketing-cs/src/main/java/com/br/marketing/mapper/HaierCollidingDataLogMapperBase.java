package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.HaierCollidingDataLog;
import com.br.marketing.entity.HaierCollidingDataLogExample;

public interface HaierCollidingDataLogMapperBase {
    int countByExample(HaierCollidingDataLogExample example);

    int deleteByExample(HaierCollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(HaierCollidingDataLog record);

    int insertSelective(HaierCollidingDataLog record);

    List<HaierCollidingDataLog> selectByExampleWithBLOBs(HaierCollidingDataLogExample example);

    List<HaierCollidingDataLog> selectByExample(HaierCollidingDataLogExample example);

    HaierCollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") HaierCollidingDataLog record, @Param("example") HaierCollidingDataLogExample example);

    int updateByExampleWithBLOBs(@Param("record") HaierCollidingDataLog record, @Param("example") HaierCollidingDataLogExample example);

    int updateByExample(@Param("record") HaierCollidingDataLog record, @Param("example") HaierCollidingDataLogExample example);

    int updateByPrimaryKeySelective(HaierCollidingDataLog record);

    int updateByPrimaryKeyWithBLOBs(HaierCollidingDataLog record);

    int updateByPrimaryKey(HaierCollidingDataLog record);
}