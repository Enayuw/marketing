package com.br.marketing.mapper;

import com.br.marketing.entity.AutoCheckResultLog;
import com.br.marketing.entity.AutoCheckResultLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AutoCheckResultLogMapperBase {
    int countByExample(AutoCheckResultLogExample example);

    int deleteByExample(AutoCheckResultLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AutoCheckResultLog record);

    int insertSelective(AutoCheckResultLog record);

    List<AutoCheckResultLog> selectByExample(AutoCheckResultLogExample example);

    AutoCheckResultLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AutoCheckResultLog record, @Param("example") AutoCheckResultLogExample example);

    int updateByExample(@Param("record") AutoCheckResultLog record, @Param("example") AutoCheckResultLogExample example);

    int updateByPrimaryKeySelective(AutoCheckResultLog record);

    int updateByPrimaryKey(AutoCheckResultLog record);
}