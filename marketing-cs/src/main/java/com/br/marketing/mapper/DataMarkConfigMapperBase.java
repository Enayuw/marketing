package com.br.marketing.mapper;

import com.br.marketing.entity.DataMarkConfig;
import com.br.marketing.entity.DataMarkConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DataMarkConfigMapperBase {
    int countByExample(DataMarkConfigExample example);

    int deleteByExample(DataMarkConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DataMarkConfig record);

    int insertSelective(DataMarkConfig record);

    List<DataMarkConfig> selectByExample(DataMarkConfigExample example);

    DataMarkConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DataMarkConfig record, @Param("example") DataMarkConfigExample example);

    int updateByExample(@Param("record") DataMarkConfig record, @Param("example") DataMarkConfigExample example);

    int updateByPrimaryKeySelective(DataMarkConfig record);

    int updateByPrimaryKey(DataMarkConfig record);
}