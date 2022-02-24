package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSyncUserMapper {
    int insertMarketingSyncUser(MarketingSyncUser syncUser);

    MarketingSyncUser selectMarketingSyncUserById(@Param("apiCode") String apiCode,@Param("id") Long id);

    int updateSyncUserStatus(@Param("apiCode") String apiCode,@Param("id") Long id,@Param("isTask")Integer isTask);

    Long minId(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate,@Param("dataType")Integer dataType, @Param("userTypes") List<String> userTypes);

    Long maxId(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate,@Param("dataType")Integer dataType, @Param("userTypes") List<String> userTypes);

    List<MarketingSyncUser> getUserById(@Param("apiCode") String apiCode, @Param("minId") Long minId,@Param("maxId") Long maxId,@Param("dataType")Integer dataType);

    MarketingSyncUser selectSynsUserByCustNumLast(@Param("apiCode") String apiCode,@Param("custNum") String custNum);
}
