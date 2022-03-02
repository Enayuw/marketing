package com.br.marketing.mapper;

import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransferFileTaskMapperBase {
    int countByExample(TransferFileTaskExample example);

    int deleteByExample(TransferFileTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TransferFileTask record);

    int insertSelective(TransferFileTask record);

    List<TransferFileTask> selectByExample(TransferFileTaskExample example);

    TransferFileTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TransferFileTask record, @Param("example") TransferFileTaskExample example);

    int updateByExample(@Param("record") TransferFileTask record, @Param("example") TransferFileTaskExample example);

    int updateByPrimaryKeySelective(TransferFileTask record);

    int updateByPrimaryKey(TransferFileTask record);
}