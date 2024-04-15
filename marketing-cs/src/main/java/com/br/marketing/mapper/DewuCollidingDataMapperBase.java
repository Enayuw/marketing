package com.br.marketing.mapper;

import com.br.marketing.entity.DewuCollidingData;
import com.br.marketing.entity.DewuCollidingDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DewuCollidingDataMapperBase {
    int countByExample(DewuCollidingDataExample example);

    int deleteByExample(DewuCollidingDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DewuCollidingData record);

    int insertSelective(DewuCollidingData record);

    List<DewuCollidingData> selectByExampleWithBLOBs(DewuCollidingDataExample example);

    List<DewuCollidingData> selectByExample(DewuCollidingDataExample example);

    DewuCollidingData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DewuCollidingData record, @Param("example") DewuCollidingDataExample example);

    int updateByExampleWithBLOBs(@Param("record") DewuCollidingData record, @Param("example") DewuCollidingDataExample example);

    int updateByExample(@Param("record") DewuCollidingData record, @Param("example") DewuCollidingDataExample example);

    int updateByPrimaryKeySelective(DewuCollidingData record);

    int updateByPrimaryKeyWithBLOBs(DewuCollidingData record);

    int updateByPrimaryKey(DewuCollidingData record);
}