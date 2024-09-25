package com.br.marketing.mapper;

import com.br.marketing.entity.SmsRecord;
import com.br.marketing.entity.SmsRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsRecordMapperBase {
    int countByExample(SmsRecordExample example);

    int deleteByExample(SmsRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsRecord record);

    int insertSelective(SmsRecord record);

    List<SmsRecord> selectByExample(SmsRecordExample example);

    SmsRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SmsRecord record, @Param("example") SmsRecordExample example);

    int updateByExample(@Param("record") SmsRecord record, @Param("example") SmsRecordExample example);

    int updateByPrimaryKeySelective(SmsRecord record);

    int updateByPrimaryKey(SmsRecord record);
}