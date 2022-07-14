package com.br.marketing.mapper;

import com.br.marketing.entity.TransferSyncReport;
import com.br.marketing.entity.TransferSyncReportExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransferSyncReportMapperBase {
    int countByExample(TransferSyncReportExample example);

    int deleteByExample(TransferSyncReportExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TransferSyncReport record);

    int insertSelective(TransferSyncReport record);

    List<TransferSyncReport> selectByExample(TransferSyncReportExample example);

    TransferSyncReport selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TransferSyncReport record, @Param("example") TransferSyncReportExample example);

    int updateByExample(@Param("record") TransferSyncReport record, @Param("example") TransferSyncReportExample example);

    int updateByPrimaryKeySelective(TransferSyncReport record);

    int updateByPrimaryKey(TransferSyncReport record);
}