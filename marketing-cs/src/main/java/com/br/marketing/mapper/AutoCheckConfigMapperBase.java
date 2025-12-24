package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckConfig;
import com.br.marketing.entity.AutoCheckConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckConfigMapperBase {
    int countByExample(AutoCheckConfigExample example);

    int deleteByExample(AutoCheckConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AutoCheckConfig record);

    int insertSelective(AutoCheckConfig record);

    List<AutoCheckConfig> selectByExample(AutoCheckConfigExample example);

    AutoCheckConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AutoCheckConfig record, @Param("example") AutoCheckConfigExample example);

    int updateByExample(@Param("record") AutoCheckConfig record, @Param("example") AutoCheckConfigExample example);

    int updateByPrimaryKeySelective(AutoCheckConfig record);

    int updateByPrimaryKey(AutoCheckConfig record);
}