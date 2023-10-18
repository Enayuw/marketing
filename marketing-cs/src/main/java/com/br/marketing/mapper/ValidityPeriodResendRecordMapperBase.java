package com.br.marketing.mapper;

import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.entity.ValidityPeriodResendRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ValidityPeriodResendRecordMapperBase {
    int countByExample(ValidityPeriodResendRecordExample example);

    int deleteByExample(ValidityPeriodResendRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ValidityPeriodResendRecord record);

    int insertSelective(ValidityPeriodResendRecord record);

    List<ValidityPeriodResendRecord> selectByExample(ValidityPeriodResendRecordExample example);

    ValidityPeriodResendRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ValidityPeriodResendRecord record, @Param("example") ValidityPeriodResendRecordExample example);

    int updateByExample(@Param("record") ValidityPeriodResendRecord record, @Param("example") ValidityPeriodResendRecordExample example);

    int updateByPrimaryKeySelective(ValidityPeriodResendRecord record);

    int updateByPrimaryKey(ValidityPeriodResendRecord record);
}