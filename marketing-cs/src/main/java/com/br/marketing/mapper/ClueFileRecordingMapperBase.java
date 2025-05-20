package com.br.marketing.mapper;

import com.br.marketing.entity.ClueFileRecording;
import com.br.marketing.entity.ClueFileRecordingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClueFileRecordingMapperBase {
    int countByExample(ClueFileRecordingExample example);

    int deleteByExample(ClueFileRecordingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ClueFileRecording record);

    int insertSelective(ClueFileRecording record);

    List<ClueFileRecording> selectByExample(ClueFileRecordingExample example);

    ClueFileRecording selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ClueFileRecording record, @Param("example") ClueFileRecordingExample example);

    int updateByExample(@Param("record") ClueFileRecording record, @Param("example") ClueFileRecordingExample example);

    int updateByPrimaryKeySelective(ClueFileRecording record);

    int updateByPrimaryKey(ClueFileRecording record);
}