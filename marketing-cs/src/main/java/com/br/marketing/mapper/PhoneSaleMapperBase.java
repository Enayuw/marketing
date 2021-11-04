package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.PhoneSaleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleMapperBase {
    int countByExample(PhoneSaleExample example);

    int deleteByExample(PhoneSaleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneSale record);

    int insertSelective(PhoneSale record);

    List<PhoneSale> selectByExample(PhoneSaleExample example);

    PhoneSale selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneSale record, @Param("example") PhoneSaleExample example);

    int updateByExample(@Param("record") PhoneSale record, @Param("example") PhoneSaleExample example);

    int updateByPrimaryKeySelective(PhoneSale record);

    int updateByPrimaryKey(PhoneSale record);
}