package com.br.marketing.mapper;

import com.br.marketing.entity.CsosPhoneSale;
import com.br.marketing.entity.CsosPhoneSaleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface CsosPhoneSaleMapperBase {
    long countByExample(CsosPhoneSaleExample example);

    int deleteByExample(CsosPhoneSaleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CsosPhoneSale record);

    int insertSelective(CsosPhoneSale record);

    List<CsosPhoneSale> selectByExample(CsosPhoneSaleExample example);

    CsosPhoneSale selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CsosPhoneSale record, @Param("example") CsosPhoneSaleExample example);

    int updateByExample(@Param("record") CsosPhoneSale record, @Param("example") CsosPhoneSaleExample example);

    int updateByPrimaryKeySelective(CsosPhoneSale record);

    int updateByPrimaryKey(CsosPhoneSale record);
}