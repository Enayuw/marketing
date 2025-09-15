package com.br.marketing.mapper;

import com.br.marketing.entity.ScoreDorisLog;
import com.br.marketing.entity.ScoreDorisLogExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ScoreDorisLogMapperBase {
    long countByExample(ScoreDorisLogExample example);

    int deleteByExample(ScoreDorisLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ScoreDorisLog record);

    int insertSelective(ScoreDorisLog record);

    List<ScoreDorisLog> selectByExample(ScoreDorisLogExample example);

    ScoreDorisLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ScoreDorisLog record, @Param("example") ScoreDorisLogExample example);

    int updateByExample(@Param("record") ScoreDorisLog record, @Param("example") ScoreDorisLogExample example);

    int updateByPrimaryKeySelective(ScoreDorisLog record);

    int updateByPrimaryKey(ScoreDorisLog record);
}