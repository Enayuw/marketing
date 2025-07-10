package com.br.marketing.mapper;

import com.br.marketing.entity.NfsFileTOBiRecord;
import com.br.marketing.entity.NfsFileTOBiRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NfsFileTOBiRecordBaseMapper {
    int countByExample(NfsFileTOBiRecordExample example);

    int deleteByExample(NfsFileTOBiRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(NfsFileTOBiRecord record);

    int insertSelective(NfsFileTOBiRecord record);

    List<NfsFileTOBiRecord> selectByExample(NfsFileTOBiRecordExample example);

    NfsFileTOBiRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") NfsFileTOBiRecord record, @Param("example") NfsFileTOBiRecordExample example);

    int updateByExample(@Param("record") NfsFileTOBiRecord record, @Param("example") NfsFileTOBiRecordExample example);

    int updateByPrimaryKeySelective(NfsFileTOBiRecord record);

    int updateByPrimaryKey(NfsFileTOBiRecord record);
}