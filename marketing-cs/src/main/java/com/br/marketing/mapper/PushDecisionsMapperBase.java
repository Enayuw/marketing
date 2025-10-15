package com.br.marketing.mapper;

import com.br.marketing.entity.PushDecisions;
import com.br.marketing.entity.PushDecisionsExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PushDecisionsMapperBase {
    int countByExample(PushDecisionsExample example);

    int deleteByExample(PushDecisionsExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PushDecisions record);

    int insertSelective(PushDecisions record);

    List<PushDecisions> selectByExample(PushDecisionsExample example);

    PushDecisions selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PushDecisions record, @Param("example") PushDecisionsExample example);

    int updateByExample(@Param("record") PushDecisions record, @Param("example") PushDecisionsExample example);

    int updateByPrimaryKeySelective(PushDecisions record);

    int updateByPrimaryKey(PushDecisions record);
}