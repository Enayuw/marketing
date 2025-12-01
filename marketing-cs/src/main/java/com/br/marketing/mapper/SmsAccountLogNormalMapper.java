package com.br.marketing.mapper;

import com.br.marketing.entity.SmsAccountLogNormal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmsAccountLogNormalMapper extends SmsAccountLogNormalMapperBase{

    List<SmsAccountLogNormal> getLineAccountLogs(@Param("groupId") Long groupId);
}