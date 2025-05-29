package com.br.marketing.mapper;

import com.br.marketing.entity.UpdatePhoneSale;
import com.br.marketing.entity.UpdatePhoneSaleExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface UpdatePhoneSaleMapperBase {
    int countByExample(UpdatePhoneSaleExample example);

    int deleteByExample(UpdatePhoneSaleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(UpdatePhoneSale record);

    int insertSelective(UpdatePhoneSale record);

    List<UpdatePhoneSale> selectByExample(UpdatePhoneSaleExample example);

    UpdatePhoneSale selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") UpdatePhoneSale record, @Param("example") UpdatePhoneSaleExample example);

    int updateByExample(@Param("record") UpdatePhoneSale record, @Param("example") UpdatePhoneSaleExample example);

    int updateByPrimaryKeySelective(UpdatePhoneSale record);

    int updateByPrimaryKey(UpdatePhoneSale record);
}