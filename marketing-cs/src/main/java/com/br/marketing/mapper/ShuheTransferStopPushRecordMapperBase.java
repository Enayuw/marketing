package com.br.marketing.mapper;

import com.br.marketing.entity.ShuheTransferStopPushRecord;
import com.br.marketing.entity.ShuheTransferStopPushRecordExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShuheTransferStopPushRecordMapperBase {
    int countByExample(ShuheTransferStopPushRecordExample example);

    int deleteByExample(ShuheTransferStopPushRecordExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ShuheTransferStopPushRecord record);

    int insertSelective(ShuheTransferStopPushRecord record);

    List<ShuheTransferStopPushRecord> selectByExample(ShuheTransferStopPushRecordExample example);

    ShuheTransferStopPushRecord selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ShuheTransferStopPushRecord record, @Param("example") ShuheTransferStopPushRecordExample example);

    int updateByExample(@Param("record") ShuheTransferStopPushRecord record, @Param("example") ShuheTransferStopPushRecordExample example);

    int updateByPrimaryKeySelective(ShuheTransferStopPushRecord record);

    int updateByPrimaryKey(ShuheTransferStopPushRecord record);
}