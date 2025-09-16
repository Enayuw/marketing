package com.br.marketing.mapper;

import com.br.marketing.entity.AiToPolicyRecord;
import com.br.marketing.entity.AiToPolicyRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AiToPolicyRecordMapperBase {
    int countByExample(AiToPolicyRecordExample example);

    int deleteByExample(AiToPolicyRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AiToPolicyRecord record);

    int insertSelective(AiToPolicyRecord record);

    List<AiToPolicyRecord> selectByExample(AiToPolicyRecordExample example);

    AiToPolicyRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AiToPolicyRecord record, @Param("example") AiToPolicyRecordExample example);

    int updateByExample(@Param("record") AiToPolicyRecord record, @Param("example") AiToPolicyRecordExample example);

    int updateByPrimaryKeySelective(AiToPolicyRecord record);

    int updateByPrimaryKey(AiToPolicyRecord record);
}