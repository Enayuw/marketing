package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleTransfer;
import com.br.marketing.entity.PhoneSaleTransferExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleTransferMapperBase {
    int countByExample(PhoneSaleTransferExample example);

    int deleteByExample(PhoneSaleTransferExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneSaleTransfer record);

    int insertSelective(PhoneSaleTransfer record);

    List<PhoneSaleTransfer> selectByExample(PhoneSaleTransferExample example);

    PhoneSaleTransfer selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneSaleTransfer record, @Param("example") PhoneSaleTransferExample example);

    int updateByExample(@Param("record") PhoneSaleTransfer record, @Param("example") PhoneSaleTransferExample example);

    int updateByPrimaryKeySelective(PhoneSaleTransfer record);

    int updateByPrimaryKey(PhoneSaleTransfer record);
}