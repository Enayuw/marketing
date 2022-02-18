package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.CallRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CallRecordMapperBase {
    int countByExample(CallRecordExample example);

    int deleteByExample(CallRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CallRecord record);

    int insertSelective(CallRecord record);

    List<CallRecord> selectByExample(CallRecordExample example);

    CallRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CallRecord record, @Param("example") CallRecordExample example);

    int updateByExample(@Param("record") CallRecord record, @Param("example") CallRecordExample example);

    int updateByPrimaryKeySelective(CallRecord record);

    int updateByPrimaryKey(CallRecord record);
}