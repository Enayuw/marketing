package com.br.marketing.mapper;

import com.br.marketing.entity.DecisionsTaskLog;
import com.br.marketing.entity.DecisionsTaskLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DecisionsTaskLogMapperBase {
    int countByExample(DecisionsTaskLogExample example);

    int deleteByExample(DecisionsTaskLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DecisionsTaskLog record);

    int insertSelective(DecisionsTaskLog record);

    List<DecisionsTaskLog> selectByExample(DecisionsTaskLogExample example);

    DecisionsTaskLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DecisionsTaskLog record, @Param("example") DecisionsTaskLogExample example);

    int updateByExample(@Param("record") DecisionsTaskLog record, @Param("example") DecisionsTaskLogExample example);

    int updateByPrimaryKeySelective(DecisionsTaskLog record);

    int updateByPrimaryKey(DecisionsTaskLog record);
}