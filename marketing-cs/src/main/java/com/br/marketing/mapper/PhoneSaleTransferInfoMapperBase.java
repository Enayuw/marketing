package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleTransferInfo;
import com.br.marketing.entity.PhoneSaleTransferInfoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleTransferInfoMapperBase {
    int countByExample(PhoneSaleTransferInfoExample example);

    int deleteByExample(PhoneSaleTransferInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneSaleTransferInfo record);

    int insertSelective(PhoneSaleTransferInfo record);

    List<PhoneSaleTransferInfo> selectByExample(PhoneSaleTransferInfoExample example);

    PhoneSaleTransferInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneSaleTransferInfo record, @Param("example") PhoneSaleTransferInfoExample example);

    int updateByExample(@Param("record") PhoneSaleTransferInfo record, @Param("example") PhoneSaleTransferInfoExample example);

    int updateByPrimaryKeySelective(PhoneSaleTransferInfo record);

    int updateByPrimaryKey(PhoneSaleTransferInfo record);
}