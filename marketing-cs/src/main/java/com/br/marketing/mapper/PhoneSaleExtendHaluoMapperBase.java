package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleExtendHaluo;
import com.br.marketing.entity.PhoneSaleExtendHaluoExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleExtendHaluoMapperBase {
    int countByExample(PhoneSaleExtendHaluoExample example);

    int deleteByExample(PhoneSaleExtendHaluoExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneSaleExtendHaluo record);

    int insertSelective(PhoneSaleExtendHaluo record);

    List<PhoneSaleExtendHaluo> selectByExample(PhoneSaleExtendHaluoExample example);

    PhoneSaleExtendHaluo selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneSaleExtendHaluo record, @Param("example") PhoneSaleExtendHaluoExample example);

    int updateByExample(@Param("record") PhoneSaleExtendHaluo record, @Param("example") PhoneSaleExtendHaluoExample example);

    int updateByPrimaryKeySelective(PhoneSaleExtendHaluo record);

    int updateByPrimaryKey(PhoneSaleExtendHaluo record);
}