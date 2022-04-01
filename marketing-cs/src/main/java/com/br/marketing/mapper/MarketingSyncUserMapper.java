package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface MarketingSyncUserMapper {
    int insertMarketingSyncUser(MarketingSyncUser syncUser);

    MarketingSyncUser selectMarketingSyncUserById(@Param("apiCode") String apiCode,@Param("id") Long id);

    int updateSyncUserStatus(@Param("apiCode") String apiCode,@Param("id") Long id,@Param("isTask")Integer isTask);

    Long minId(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate,@Param("dataType")Integer dataType, @Param("userTypes") List<String> userTypes);

    Long maxId(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate,@Param("dataType")Integer dataType, @Param("userTypes") List<String> userTypes);

    List<MarketingSyncUser> getUserById(@Param("apiCode") String apiCode, @Param("minId") Long minId,@Param("maxId") Long maxId,@Param("dataType")Integer dataType);

    MarketingSyncUser selectSynsUserByCustNumLast(@Param("apiCode") String apiCode,@Param("custNum") String custNum);

    /**
     * 根据客户编号修改上传详情表数据为剔除状态
     * @param apiCode
     * @param uIds
     * @return
     */
    int updateSyncUserCaseEffective(@Param("apiCode") String apiCode,@Param("uIds") Set<String> uIds);

    List<MarketingSyncUser> getSyncUserLastByCustNums(@Param("apiCode") String apiCode,@Param("custNums") List<String> custNums);
}
