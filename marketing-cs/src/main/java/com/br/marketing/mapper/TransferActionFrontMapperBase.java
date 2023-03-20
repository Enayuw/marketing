package com.br.marketing.mapper;

import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransferActionFrontMapperBase {
    long countByExample(TransferActionFrontExample example);

    int deleteByExample(TransferActionFrontExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TransferActionFront record);

    int insertSelective(TransferActionFront record);

    List<TransferActionFront> selectByExample(TransferActionFrontExample example);

    TransferActionFront selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TransferActionFront record, @Param("example") TransferActionFrontExample example);

    int updateByExample(@Param("record") TransferActionFront record, @Param("example") TransferActionFrontExample example);

    int updateByPrimaryKeySelective(TransferActionFront record);

    int updateByPrimaryKey(TransferActionFront record);
}