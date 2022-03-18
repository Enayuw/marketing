package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleExtendShuhe;
import com.br.marketing.entity.PhoneSaleExtendShuheExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleExtendShuheMapperBase {
    long countByExample(PhoneSaleExtendShuheExample example);

    int deleteByExample(PhoneSaleExtendShuheExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneSaleExtendShuhe record);

    int insertSelective(PhoneSaleExtendShuhe record);

    List<PhoneSaleExtendShuhe> selectByExample(PhoneSaleExtendShuheExample example);

    PhoneSaleExtendShuhe selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneSaleExtendShuhe record, @Param("example") PhoneSaleExtendShuheExample example);

    int updateByExample(@Param("record") PhoneSaleExtendShuhe record, @Param("example") PhoneSaleExtendShuheExample example);

    int updateByPrimaryKeySelective(PhoneSaleExtendShuhe record);

    int updateByPrimaryKey(PhoneSaleExtendShuhe record);
}