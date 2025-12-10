package com.br.marketing.mapper;

import com.br.marketing.entity.SmsVendorInfoNormal;
import com.br.marketing.entity.SmsVendorInfoNormalExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsVendorInfoNormalMapperBase {
    int countByExample(SmsVendorInfoNormalExample example);

    int deleteByExample(SmsVendorInfoNormalExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SmsVendorInfoNormal record);

    int insertSelective(SmsVendorInfoNormal record);

    List<SmsVendorInfoNormal> selectByExample(SmsVendorInfoNormalExample example);

    SmsVendorInfoNormal selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SmsVendorInfoNormal record, @Param("example") SmsVendorInfoNormalExample example);

    int updateByExample(@Param("record") SmsVendorInfoNormal record, @Param("example") SmsVendorInfoNormalExample example);

    int updateByPrimaryKeySelective(SmsVendorInfoNormal record);

    int updateByPrimaryKey(SmsVendorInfoNormal record);
}