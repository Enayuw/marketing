package com.br.marketing.mapper;

import com.br.marketing.entity.DataCompare;
import com.br.marketing.entity.DataCompareExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataCompareMapper {
    int countByExample(DataCompareExample example);

    int deleteByExample(DataCompareExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataCompare record);

    int insertSelective(DataCompare record);

    List<DataCompare> selectByExampleWithBLOBs(DataCompareExample example);

    List<DataCompare> selectByExample(DataCompareExample example);

    DataCompare selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataCompare record, @Param("example") DataCompareExample example);

    int updateByExampleWithBLOBs(@Param("record") DataCompare record, @Param("example") DataCompareExample example);

    int updateByExample(@Param("record") DataCompare record, @Param("example") DataCompareExample example);

    int updateByPrimaryKeySelective(DataCompare record);

    int updateByPrimaryKeyWithBLOBs(DataCompare record);

    int updateByPrimaryKey(DataCompare record);
}