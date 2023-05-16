package com.br.marketing.mapper;

import com.br.marketing.entity.DidiData;
import com.br.marketing.entity.DidiDataExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DidiDataMapperBase {
    int countByExample(DidiDataExample example);

    int deleteByExample(DidiDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DidiData record);

    int insertSelective(DidiData record);

    List<DidiData> selectByExample(DidiDataExample example);

    DidiData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DidiData record, @Param("example") DidiDataExample example);

    int updateByExample(@Param("record") DidiData record, @Param("example") DidiDataExample example);

    int updateByPrimaryKeySelective(DidiData record);

    int updateByPrimaryKey(DidiData record);
}