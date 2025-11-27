package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecordConfig;
import com.br.marketing.entity.CallRecordConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CallRecordConfigMapperBase {
    int countByExample(CallRecordConfigExample example);

    int deleteByExample(CallRecordConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CallRecordConfig record);

    int insertSelective(CallRecordConfig record);

    List<CallRecordConfig> selectByExample(CallRecordConfigExample example);

    CallRecordConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CallRecordConfig record, @Param("example") CallRecordConfigExample example);

    int updateByExample(@Param("record") CallRecordConfig record, @Param("example") CallRecordConfigExample example);

    int updateByPrimaryKeySelective(CallRecordConfig record);

    int updateByPrimaryKey(CallRecordConfig record);
}