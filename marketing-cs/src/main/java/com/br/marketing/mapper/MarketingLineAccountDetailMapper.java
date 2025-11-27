package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface MarketingLineAccountDetailMapper extends MarketingLineAccountDetailMapperBase{

    List<Long> selectLineIfExist(@Param("gatewayIds") List<Long> gatewayIds, @Param("configId") Long configId);

    Long selectCount(@Param("gateWayId") Long gatewayId);
}
