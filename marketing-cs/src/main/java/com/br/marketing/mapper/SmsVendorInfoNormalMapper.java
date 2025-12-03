package com.br.marketing.mapper;

import com.br.marketing.entity.SmsVendorInfoNormal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsVendorInfoNormalMapper  extends SmsVendorInfoNormalMapperBase{

    List<SmsVendorInfoNormal> selectList();

    SmsVendorInfoNormal selectByVendorId(@Param("vendorId") Long vendorId);

    void updateInfoById(@Param("vendorPrimaryId") Long vendorPrimaryId,
                        @Param("vendorName") String vendorName,
                        @Param("opeStatus") Integer opeStatus);
}