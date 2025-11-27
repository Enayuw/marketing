package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecordLLMResultV2;
import com.br.marketing.entity.CallRecordLLMResultV2Example;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CallRecordLLMResultV2MapperBase {
    int countByExample(CallRecordLLMResultV2Example example);

    int deleteByExample(CallRecordLLMResultV2Example example);

    int deleteByPrimaryKey(Long id);

    int insert(CallRecordLLMResultV2 record);

    int insertSelective(CallRecordLLMResultV2 record);

    List<CallRecordLLMResultV2> selectByExample(CallRecordLLMResultV2Example example);

    CallRecordLLMResultV2 selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CallRecordLLMResultV2 record, @Param("example") CallRecordLLMResultV2Example example);

    int updateByExample(@Param("record") CallRecordLLMResultV2 record, @Param("example") CallRecordLLMResultV2Example example);

    int updateByPrimaryKeySelective(CallRecordLLMResultV2 record);

    int updateByPrimaryKey(CallRecordLLMResultV2 record);
}