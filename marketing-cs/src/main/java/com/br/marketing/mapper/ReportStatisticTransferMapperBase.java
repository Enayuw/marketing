package com.br.marketing.mapper;

import com.br.marketing.entity.ReportStatisticTransfer;
import com.br.marketing.entity.ReportStatisticTransferExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportStatisticTransferMapperBase {
    int countByExample(ReportStatisticTransferExample example);

    int deleteByExample(ReportStatisticTransferExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ReportStatisticTransfer record);

    int insertSelective(ReportStatisticTransfer record);

    List<ReportStatisticTransfer> selectByExample(ReportStatisticTransferExample example);

    ReportStatisticTransfer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ReportStatisticTransfer record, @Param("example") ReportStatisticTransferExample example);

    int updateByExample(@Param("record") ReportStatisticTransfer record, @Param("example") ReportStatisticTransferExample example);

    int updateByPrimaryKeySelective(ReportStatisticTransfer record);

    int updateByPrimaryKey(ReportStatisticTransfer record);
}