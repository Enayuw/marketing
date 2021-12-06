package com.br.marketing.mapper;

import com.br.marketing.entity.HaierData;
import com.br.marketing.entity.HaierDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierDataMapperBase {
    int countByExample(HaierDataExample example);

    int deleteByExample(HaierDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(HaierData record);

    int insertSelective(HaierData record);

    List<HaierData> selectByExampleWithBLOBs(HaierDataExample example);

    List<HaierData> selectByExample(HaierDataExample example);

    HaierData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") HaierData record, @Param("example") HaierDataExample example);

    int updateByExampleWithBLOBs(@Param("record") HaierData record, @Param("example") HaierDataExample example);

    int updateByExample(@Param("record") HaierData record, @Param("example") HaierDataExample example);

    int updateByPrimaryKeySelective(HaierData record);

    int updateByPrimaryKeyWithBLOBs(HaierData record);

    int updateByPrimaryKey(HaierData record);
}