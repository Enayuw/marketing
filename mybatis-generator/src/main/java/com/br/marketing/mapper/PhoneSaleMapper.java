package com.br.marketing.mapper;

import com.br.marketing.entity.PhoneSale;
import com.br.marketing.entity.PhoneSaleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface PhoneSaleMapper {
    int countByExample(PhoneSaleExample example);

    int deleteByExample(PhoneSaleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PhoneSale record);

    int insertSelective(PhoneSale record);

    List<PhoneSale> selectByExampleWithBLOBs(PhoneSaleExample example);

    List<PhoneSale> selectByExample(PhoneSaleExample example);

    PhoneSale selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PhoneSale record, @Param("example") PhoneSaleExample example);

    int updateByExampleWithBLOBs(@Param("record") PhoneSale record, @Param("example") PhoneSaleExample example);

    int updateByExample(@Param("record") PhoneSale record, @Param("example") PhoneSaleExample example);

    int updateByPrimaryKeySelective(PhoneSale record);

    int updateByPrimaryKeyWithBLOBs(PhoneSale record);

    int updateByPrimaryKey(PhoneSale record);
}