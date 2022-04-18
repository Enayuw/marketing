package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.entity.PhoneSaleExtendInfoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleExtendInfoMapperBase {
    int countByExample(PhoneSaleExtendInfoExample example);

    int deleteByExample(PhoneSaleExtendInfoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneSaleExtendInfo record);

    int insertSelective(PhoneSaleExtendInfo record);

    List<PhoneSaleExtendInfo> selectByExample(PhoneSaleExtendInfoExample example);

    PhoneSaleExtendInfo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneSaleExtendInfo record, @Param("example") PhoneSaleExtendInfoExample example);

    int updateByExample(@Param("record") PhoneSaleExtendInfo record, @Param("example") PhoneSaleExtendInfoExample example);

    int updateByPrimaryKeySelective(PhoneSaleExtendInfo record);

    int updateByPrimaryKey(PhoneSaleExtendInfo record);
}