package com.br.marketing.mapper;

import com.br.marketing.entity.DidiCallRecord;
import com.br.marketing.entity.DidiCallRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DidiCallRecordMapperBase {
    int countByExample(DidiCallRecordExample example);

    int deleteByExample(DidiCallRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DidiCallRecord record);

    int insertSelective(DidiCallRecord record);

    List<DidiCallRecord> selectByExample(DidiCallRecordExample example);

    DidiCallRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DidiCallRecord record, @Param("example") DidiCallRecordExample example);

    int updateByExample(@Param("record") DidiCallRecord record, @Param("example") DidiCallRecordExample example);

    int updateByPrimaryKeySelective(DidiCallRecord record);

    int updateByPrimaryKey(DidiCallRecord record);
}