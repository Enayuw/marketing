package com.br.marketing.mapper;

import com.br.marketing.entity.TaikangDingDingDataRecord;
import com.br.marketing.entity.TaikangDingDingDataRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaikangDingDingDataRecordMapperBase {
    int countByExample(TaikangDingDingDataRecordExample example);

    int deleteByExample(TaikangDingDingDataRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TaikangDingDingDataRecord record);

    int insertSelective(TaikangDingDingDataRecord record);

    List<TaikangDingDingDataRecord> selectByExample(TaikangDingDingDataRecordExample example);

    TaikangDingDingDataRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TaikangDingDingDataRecord record, @Param("example") TaikangDingDingDataRecordExample example);

    int updateByExample(@Param("record") TaikangDingDingDataRecord record, @Param("example") TaikangDingDingDataRecordExample example);

    int updateByPrimaryKeySelective(TaikangDingDingDataRecord record);

    int updateByPrimaryKey(TaikangDingDingDataRecord record);
}