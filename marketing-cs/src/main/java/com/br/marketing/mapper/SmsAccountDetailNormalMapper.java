package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;

public interface SmsAccountDetailNormalMapper extends SmsAccountDetailNormalMapperBase{

    Long selectCount(@Param("vendorId") Long vendorId, @Param("channelId") Long channelId);
}