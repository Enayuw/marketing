package com.br.marketing.mapper;

import com.br.marketing.entity.CarClueExecuteRecording;
import com.br.marketing.entity.CarClueExecuteRecordingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CarClueExecuteRecordingMapperBase {
    int countByExample(CarClueExecuteRecordingExample example);

    int deleteByExample(CarClueExecuteRecordingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CarClueExecuteRecording record);

    int insertSelective(CarClueExecuteRecording record);

    List<CarClueExecuteRecording> selectByExample(CarClueExecuteRecordingExample example);

    CarClueExecuteRecording selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CarClueExecuteRecording record, @Param("example") CarClueExecuteRecordingExample example);

    int updateByExample(@Param("record") CarClueExecuteRecording record, @Param("example") CarClueExecuteRecordingExample example);

    int updateByPrimaryKeySelective(CarClueExecuteRecording record);

    int updateByPrimaryKey(CarClueExecuteRecording record);
}