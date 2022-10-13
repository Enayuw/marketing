package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
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

    List<MarketingSyncUser> getNewestByCustNums(@Param("apiCode") String apiCode,@Param("custNums") Set<String> custNums);

    /**
     * 2022/7/14 11:20
     * 获取案件集合中最大时间
     *
     * @param dateTimeEnd 截止时间
     * @return list
     */
    List<MarketingSyncUser> getSyncUserTimeMaxByCustNums(@Param("apiCode") String apiCode
            , @Param("custNums") Set<String> custNums
            , @Param("userType") String userType
            , @Param("dateTimeEnd") String dateTimeEnd);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @param freeUserTypeAndDateMap 自由定义的时间与userType，key userType；value dateSet
     * @return list
     */
    List<MarketingSyncUser> getFreeUserTypeAndDateAllFieldList(@Param("apiCode") String apiCode
            , @Param("custNumSet") Set<String> custNumSet
            , @Param("freeUserTypeAndDateMap") Map<String, Set<String>> freeUserTypeAndDateMap);

    /**
     * 2022/9/22 11:20
     * 获取自定义日期与场景下的上传信息
     *
     * @param freeUserTypeAndDateMap 自由定义的时间与userType，key userType；value dateSet
     * @return list
     */
    List<MarketingSyncUser> getFreeUserTypeAndDateList(@Param("apiCode") String apiCode
            , @Param("custNumSet") Set<String> custNumSet
            , @Param("freeUserTypeAndDateMap") Map<String, Set<String>> freeUserTypeAndDateMap);

    /**
     * 2022/10/10 11:20
     * 批量获取最新时间数据信息手机号
     *
     * @return list
     */
    List<MarketingSyncUser> getCellByCustNumsAndMaxCreateTime(@Param("apiCode") String apiCode
            , @Param("list") List<MarketingTransferSyncUser> list);

}
