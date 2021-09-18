package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncUser;
import org.apache.ibatis.annotations.Param;

public interface MarketingSyncUserMapper {
    int insertMarketingSyncUser(MarketingSyncUser syncUser);

    MarketingSyncUser selectMarketingSyncUserById(@Param("apiCode") String apiCode,@Param("id") Long id);
}
