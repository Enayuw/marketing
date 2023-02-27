package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSaleIbu;
import com.br.marketing.entity.PhoneSaleIbuExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PhoneSaleIbuMapperBase {
    int countByExample(PhoneSaleIbuExample example);

    int deleteByExample(PhoneSaleIbuExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneSaleIbu record);

    int insertSelective(PhoneSaleIbu record);

    List<PhoneSaleIbu> selectByExample(PhoneSaleIbuExample example);

    PhoneSaleIbu selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneSaleIbu record, @Param("example") PhoneSaleIbuExample example);

    int updateByExample(@Param("record") PhoneSaleIbu record, @Param("example") PhoneSaleIbuExample example);

    int updateByPrimaryKeySelective(PhoneSaleIbu record);

    int updateByPrimaryKey(PhoneSaleIbu record);
}