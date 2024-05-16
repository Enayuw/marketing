package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengRuleScoreRecord;
import com.br.marketing.entity.XieChengRuleScoreRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengRuleScoreRecordMapperBase {
    int countByExample(XieChengRuleScoreRecordExample example);

    int deleteByExample(XieChengRuleScoreRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengRuleScoreRecord record);

    int insertSelective(XieChengRuleScoreRecord record);

    List<XieChengRuleScoreRecord> selectByExample(XieChengRuleScoreRecordExample example);

    XieChengRuleScoreRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengRuleScoreRecord record, @Param("example") XieChengRuleScoreRecordExample example);

    int updateByExample(@Param("record") XieChengRuleScoreRecord record, @Param("example") XieChengRuleScoreRecordExample example);

    int updateByPrimaryKeySelective(XieChengRuleScoreRecord record);

    int updateByPrimaryKey(XieChengRuleScoreRecord record);
}