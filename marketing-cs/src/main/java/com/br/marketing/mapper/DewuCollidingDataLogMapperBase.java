package com.br.marketing.mapper;

import com.br.marketing.entity.DewuCollidingDataLog;
import com.br.marketing.entity.DewuCollidingDataLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DewuCollidingDataLogMapperBase {
    int countByExample(DewuCollidingDataLogExample example);

    int deleteByExample(DewuCollidingDataLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DewuCollidingDataLog record);

    int insertSelective(DewuCollidingDataLog record);

    List<DewuCollidingDataLog> selectByExampleWithBLOBs(DewuCollidingDataLogExample example);

    List<DewuCollidingDataLog> selectByExample(DewuCollidingDataLogExample example);

    DewuCollidingDataLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DewuCollidingDataLog record, @Param("example") DewuCollidingDataLogExample example);

    int updateByExampleWithBLOBs(@Param("record") DewuCollidingDataLog record, @Param("example") DewuCollidingDataLogExample example);

    int updateByExample(@Param("record") DewuCollidingDataLog record, @Param("example") DewuCollidingDataLogExample example);

    int updateByPrimaryKeySelective(DewuCollidingDataLog record);

    int updateByPrimaryKeyWithBLOBs(DewuCollidingDataLog record);

    int updateByPrimaryKey(DewuCollidingDataLog record);
}