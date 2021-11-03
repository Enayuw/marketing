package com.br.marketing.mapper;

import com.br.marketing.entity.SoleOptLog;
import com.br.marketing.entity.SoleOptLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SoleOptLogMapperBase {
    int countByExample(SoleOptLogExample example);

    int deleteByExample(SoleOptLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SoleOptLog record);

    int insertSelective(SoleOptLog record);

    List<SoleOptLog> selectByExample(SoleOptLogExample example);

    SoleOptLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SoleOptLog record, @Param("example") SoleOptLogExample example);

    int updateByExample(@Param("record") SoleOptLog record, @Param("example") SoleOptLogExample example);

    int updateByPrimaryKeySelective(SoleOptLog record);

    int updateByPrimaryKey(SoleOptLog record);
}