package com.br.marketing.mapper;

import com.br.marketing.entity.SmsBaseInfoNormal;
import com.br.marketing.entity.SmsBaseInfoNormalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsBaseInfoNormalMapperBase {
    int countByExample(SmsBaseInfoNormalExample example);

    int deleteByExample(SmsBaseInfoNormalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsBaseInfoNormal record);

    int insertSelective(SmsBaseInfoNormal record);

    List<SmsBaseInfoNormal> selectByExample(SmsBaseInfoNormalExample example);

    SmsBaseInfoNormal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SmsBaseInfoNormal record, @Param("example") SmsBaseInfoNormalExample example);

    int updateByExample(@Param("record") SmsBaseInfoNormal record, @Param("example") SmsBaseInfoNormalExample example);

    int updateByPrimaryKeySelective(SmsBaseInfoNormal record);

    int updateByPrimaryKey(SmsBaseInfoNormal record);
}