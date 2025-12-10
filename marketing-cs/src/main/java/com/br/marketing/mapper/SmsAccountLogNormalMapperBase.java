package com.br.marketing.mapper;

import com.br.marketing.entity.SmsAccountLogNormal;
import com.br.marketing.entity.SmsAccountLogNormalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsAccountLogNormalMapperBase {
    int countByExample(SmsAccountLogNormalExample example);

    int deleteByExample(SmsAccountLogNormalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsAccountLogNormal record);

    int insertSelective(SmsAccountLogNormal record);

    List<SmsAccountLogNormal> selectByExample(SmsAccountLogNormalExample example);

    SmsAccountLogNormal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SmsAccountLogNormal record, @Param("example") SmsAccountLogNormalExample example);

    int updateByExample(@Param("record") SmsAccountLogNormal record, @Param("example") SmsAccountLogNormalExample example);

    int updateByPrimaryKeySelective(SmsAccountLogNormal record);

    int updateByPrimaryKey(SmsAccountLogNormal record);
}