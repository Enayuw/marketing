package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecording;
import com.br.marketing.entity.CallRecordingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CallRecordingMapperBase {
    int countByExample(CallRecordingExample example);

    int deleteByExample(CallRecordingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CallRecording record);

    int insertSelective(CallRecording record);

    List<CallRecording> selectByExample(CallRecordingExample example);

    CallRecording selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CallRecording record, @Param("example") CallRecordingExample example);

    int updateByExample(@Param("record") CallRecording record, @Param("example") CallRecordingExample example);

    int updateByPrimaryKeySelective(CallRecording record);

    int updateByPrimaryKey(CallRecording record);
}