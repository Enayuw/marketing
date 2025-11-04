package com.br.marketing.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingSmsAccountDetailMapper extends MarketingSmsAccountDetailMapperBase{

    List<Long> selectChannelIfExist(@Param("channelIds") List<Long> channelIds, @Param("configId") Long configId);

    Long selectCount(@Param("vendorId") Long vendorId, @Param("channelId") Long channelId);

}
