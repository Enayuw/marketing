package com.br.marketing.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingSmsAccountDetailMapper extends MarketingSmsAccountDetailMapperBase{

    List<Integer> selectChannelIfExist(@Param("channelIds") List<Integer> channelIds);

}
