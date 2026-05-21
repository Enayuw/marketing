package com.br.marketing.mapper;

import com.br.marketing.bo.CellValidityPeriodBO;
import com.br.marketing.entity.*;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MarketingSyncUserMapper {
    int insertMarketingSyncUser(MarketingSyncUser syncUser);

    MarketingSyncUser selectMarketingSyncUserById(@Param("apiCode") String apiCode, @Param("id") Long id);

    int updateSyncUserStatus(@Param("apiCode") String apiCode, @Param("id") Long id, @Param("isTask") Integer isTask);

    Long minId(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate, @Param("dataType") Integer dataType, @Param("userTypes") List<String> userTypes);

    Long maxId(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate, @Param("dataType") Integer dataType, @Param("userTypes") List<String> userTypes);

    List<MarketingSyncUser> getUserById(@Param("apiCode") String apiCode, @Param("minId") Long minId, @Param("maxId") Long maxId, @Param("dataType") Integer dataType);

    MarketingSyncUser selectSynsUserByCustNumLast(@Param("apiCode") String apiCode, @Param("custNum") String custNum);
    MarketingSyncUser selectSynsUserByCustNumLastWithStatus(@Param("apiCode") String apiCode, @Param("custNum") String custNum);

    MarketingSyncUser selectSynsUserByCellLast(@Param("apiCode") String apiCode, @Param("cell") String cell);

    /**
     * 根据客户编号修改上传详情表数据为剔除状态
     *
     * @param apiCode
     * @param uIds
     * @return
     */
    int updateSyncUserCaseEffective(@Param("apiCode") String apiCode, @Param("uIds") Set<String> uIds);

    List<MarketingSyncUser> getSyncUserLastByCustNums(@Param("apiCode") String apiCode, @Param("custNums") List<String> custNums);

    /**
     * 2023-03-24 1:49
     * 根据案件编号集合获取正常状态的
     * 最新上传原始数据部分属性(部分列)列表
     */
    List<MarketingSyncUser> getSyncUserLastByCustNumsAndStatus(@Param("apiCode") String apiCode, @Param("custNums") Set<String> custs);

    List<MarketingSyncUser> getSyncUserLastByCustNumsAndStatusAndDate(@Param("apiCode") String apiCode, @Param("custNums") Set<String> custs, @Param("limitDate") String limitDate);

    List<MarketingSyncUser> getSyncUserLastByInAppletDateList(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList
            , @Param("transferSyncUserList") List<MarketingTransferSyncUser> transferSyncUserList);

    List<MarketingSyncUser> getSyncUserLastByInAppletDateUserTypeList(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList
            , @Param("transferSyncUserList") List<MarketingTransferSyncUser> transferSyncUserList);

    List<MarketingSyncUser> getSyncUserLastByNotInAppletDateList(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList
            , @Param("transferSyncUserList") List<MarketingTransferSyncUser> transferSyncUserList);

    List<MarketingSyncUser> getSyncUserLastByNotInAppletDateUserTypeList(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList
            , @Param("transferSyncUserList") List<MarketingTransferSyncUser> transferSyncUserList, @Param("limitDate") String limitDate);

    List<MarketingSyncUser> getNewestByCustNums(@Param("apiCode") String apiCode, @Param("custNums") Set<String> custNums);

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
            , @Param("set") Set<String> set);

    /**
     * 2022/10/10 11:20
     * 根据手机号 批量获取最新时间上传数据信息
     *
     * @return list
     */
    List<MarketingSyncUser> getCellByCellAndMaxAppletTime(@Param("apiCode") String apiCode
            , @Param("cellSet") Set<String> cellSet);


    /**
     * 分页获取上传数据，appletdate区间
     *
     * @param apiCode
     * @param startDate
     * @param endDate
     * @param minId
     * @return
     */
    List<MarketingSyncUser> getSyncUserByAppletDateRange(@Param("apiCode") String apiCode, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("minId") Long minId);


    /**
     * 分页获取上传数据，appletdate精确到天
     *
     * @param apiCode
     * @param executeDate
     * @param minId
     * @return
     */
    List<MarketingSyncUser> getSyncUserByAppletDateAndUserType(@Param("apiCode") String apiCode, @Param("executeDate") String executeDate, @Param("minId") Long minId, @Param("userType") String userType);

    /**
     * 获取appletDate日期集合
     *
     * @param apiCode
     * @param startDate
     * @param endDate
     * @return
     */
    List<String> getAppletDateByUserType(@Param("apiCode") String apiCode, @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("userType") String userType);

    /**
     * 获取案件的上传时间
     *
     * @param syncUser 上传信息
     * @return AppletTime 、createTime
     * @author Guo Zeqiang
     * @dateTime 2023/2/09 10:52
     */
    MarketingSyncUser getAppletTimeBySyncUser(@Param("syncUser") MarketingSyncUser syncUser);

    /**
     * 根据配置信息，查询apiCode userType 时间为 3个维度下最新一条数据
     *
     * @param collectRequestDate
     * @param marketingTransferSyncUser
     * @return
     */
    MarketingSyncUser selectInAppletDate(@Param("collectRequestDate") List<MarketingDataValidConfig> collectRequestDate, @Param("marketingTransferSyncUser") MarketingTransferSyncUser marketingTransferSyncUser);

    /**
     * 根据配置信息，查询【非】apiCode userType 时间为 3个维度下最新一条数据
     *
     * @param collectRequestDate
     * @param marketingTransferSyncUser
     * @return
     */
    MarketingSyncUser selectNotInAppletDate(@Param("collectRequestDate") List<MarketingDataValidConfig> collectRequestDate, @Param("marketingTransferSyncUser") MarketingTransferSyncUser marketingTransferSyncUser);

    /**
     * 根据id和日期获取上传数据
     *
     * @param apiCode
     * @param minId
     * @param pageSize
     * @return
     */
    List<MarketingSyncUser> getNewSyncUserByDate(
            @Param("apiCode") String apiCode,
            @Param("appletDate") String appletDate,
            @Param("userType") String userType,
            @Param("pageSize") Integer pageSize,
            @Param("minId") Long minId);

    Integer countByAppletDate(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate);

    List<MarketingSyncUser> getNewSyncUserByCustNumtikv_(@Param("apiCode") String apiCode, @Param("custNums") List<String> custNums, @Param("dateBegin") String dateBegin, @Param("dateEnd") String dateEnd);

    String getMinAppletDate(@Param("apiCode") String apiCode);

    /**
     * 2023-05-12 15:57
     * 根据案件编号获取最新手机号
     */
    List<MarketingSyncUser> getCellLastByCustNums(@Param("apiCode") String apiCode
            , @Param("custNums") Set<String> custNumSet);

    MarketingSyncUser getUserLastByCell(@Param("apiCode") String apiCode
            , @Param("cell") String cell);


    List<MarketingSyncUser> getCellByAppletDateAndUserType(@Param("apiCode") String apiCode, @Param("appletDate") String executeDate, @Param("minId") Long minId, @Param("userType") String userType);

    List<MarketingSyncUser> getSyncUserByAppletDatePage(@Param("apiCode") String apiCode, @Param("appletDate") String executeDate, @Param("userType") String userType, @Param("limitStart") Integer limitStart);

    /**
     * 2023-07-13 20:15
     * 根据手机号+有效期配置获取上传数据
     *
     * @param cellValidityPeriodBOList 封装类
     */
    List<MarketingSyncUser> getSyncUserLastByCellAndInAppletDateUserTypeList(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList
            , @Param("cellValidityPeriodBOList") List<CellValidityPeriodBO> cellValidityPeriodBOList);

    /**
     * 2023-07-13 20:15
     * 根据手机号+有效期配置获取上传数据
     */
    List<MarketingSyncUser> getSyncUserLastByCellAndInAppletDatList(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList
            , @Param("cellSet") Set<String> cellSet);

    /**
     * 2023-07-13 20:15
     * 根据案件编号+有效期配置获取上传数据
     */
    List<MarketingSyncUser> getSyncUserLastByCustNumAndInAppletDatList(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList
            , @Param("custNumSet") Set<String> custNumSet);

    /**
     * 修改数据状态
     *
     * @param apiCode
     * @param custNum
     * @param appletDate
     * @return
     */
    Integer updateStatus(@Param("apiCode") String apiCode, @Param("custNum") String custNum, @Param("appletDate") String appletDate);


    /**
     * 查找指定上传日期批次号集合
     *
     * @param apiCode    apiCode
     * @param appletDate 入库日期
     * @return CusBatch 集合
     */
    List<String> findCusBatchByAppletDatePage(@Param("apiCode") String apiCode
            , @Param("cusBatch") String cusBatch
            , @Param("appletDate") String appletDate
            , @Param("pageSize") int pageSize);

    /**
     * 根据案件编号+有效期配置获取上传数据
     *
     * @param apiCode    apiCode
     * @param configList 有效配置
     * @param custNumSet custNum集合
     * @return {@link List }<{@link MarketingSyncUser }>
     * @author senyang.zheng
     * @date 2023/10/07
     */
    List<MarketingSyncUser> getSyncUserByCustNumAndAppletDateList(@Param("apiCode") String apiCode,
                                                                  @Param("configList") List<MarketingDataValidConfig> configList,
                                                                  @Param("custNumSet") Set<String> custNumSet);

    /**
     * 根据手机号+有效期配置获取上传数据
     *
     * @param apiCode    apiCode
     * @param configList 有效配置
     * @param cellSet    cell集合
     * @return {@link List }<{@link MarketingSyncUser }>
     * @author senyang.zheng
     * @date 2023/12/08
     */
    List<MarketingSyncUser> getSyncUserByCellAndAppletDateList(@Param("apiCode") String apiCode,
                                                               @Param("configList") List<MarketingDataValidConfig> configList,
                                                               @Param("cellSet") Set<String> cellSet);

    /**
     * 根据案件编号+定制化 有效期配置获取上传数据
     *
     * @param apiCode    api代码
     * @param configList 有效期配置
     * @param custNumSet custNum集合
     * @return {@link List }<{@link MarketingSyncUser }>
     * @author senyang.zheng
     * @date 2024/01/13
     */
    List<MarketingSyncUser> getSyncUserByCustNumAndTaskIdsList(@Param("apiCode") String apiCode,
                                                               @Param("configList") List<MarketingCustomizeDataValidConfig> configList,
                                                               @Param("custNumSet") Set<String> custNumSet);


    /**
     * 2024-03-08 9:29
     * 根据请求批次号获取批次号内的全部数据
     *
     * @param apiCode      code
     * @param appletDate   上传时间
     * @param requestBatch 请求批次号
     * @param userTypeSet  场景集合
     */
    List<MarketingSyncUser> selectSyncUserByRequestBatchList(@Param("apiCode") String apiCode
            , @Param("requestBatch") String requestBatch
            , @Param("userTypeSet") Set<String> userTypeSet
            , @Param("appletDate") String appletDate);


    /**
     * 通过条件获取cell在不同apiCode对应表中的数据
     * @Author yu.xia@brgroup.com
     * @Date 2024/4/13 11:50
     * @param appletTimeStart
     * @param appletTimeEnd
     * @param apiCode
     * @param userTypeList
     * @param cell
     * @return List<MarketingSyncUser>
     */
    List<MarketingSyncUserCell> selectSyncUserByCelltikv_(@Param("appletTimeStart") String appletTimeStart
            , @Param("appletTimeEnd") String appletTimeEnd
            , @Param("apiCode") String apiCode
            , @Param("userTypeList") List<String> userTypeList
            , @Param("cell") String cell
            , @Param("orderField") String orderField
            , @Param("descField") String descField);


    Integer existUploadTable(@Param("tableNm") String tableNm);

    List<MarketingSyncUser> noDesUploadByMinIdtikv_(@Param("apiCode") String apiCode, @Param("minId") Long minId);

    Long noDesUploadByMinIdtiflash_(@Param("apiCode") String apiCode);

    Integer updateSqlByNoDestikv_(@Param("updateSql") String updateSql);


    List<MarketingSyncUser> getSyncUserByRequestBatch(@Param("apiCode") String apiCode, @Param("requestBatch") String requestBatch);

    List<MarketingSyncUser> getSyncUserByCondition(
            @Param("apiCode") String apiCode,
            @Param("requestBatch") String requestBatch
    );

    List<MarketingSyncUser> getSyncUserByCusBatch(@Param("apiCode") String apiCode,
                                                  @Param("cusBatch") String cusBatch,
                                                  @Param("minId") Long minId,
                                                  @Param("createDate") String createDate,
                                                  @Param("pageSize") Integer pageSize);

    MarketingSyncUser getMarketingSyncByCusBatch(@Param("apiCode") String apiCode,
                               @Param("cusBatch") String cusBatch,
                               @Param("userType") String userType,
                               @Param("appletDate") String appletDate);
    MarketingSyncUser getMarketingSyncByAppletDateAndUserType(@Param("apiCode") String apiCode,
                               @Param("userType") String userType,
                               @Param("appletDate") String appletDate);

    Set<String> getCustNumSetByAppletDateInterval(
            @Param("apiCode") String apiCode,
            @Param("custNums") List<String> custNums,
            @Param("appletDateStart") String appletDateStart,
            @Param("appletDateEnd") String appletDateEnd
    );

    /**
     * 2024-08-09 16:53
     * 根据案件编号和上传时间查询扩展字段
     */
    List<MarketingSyncUser> getReserveFieldByCustNumAndAppletDateList(@Param("apiCode") String apiCode,
                                                                      @Param("custNumSet") Set<String> custNumSet,
                                                                      @Param("appletDateSet") Set<String> appletDateSet);

    int updateReserveFieldByPrimaryKey(MarketingSyncUser record);

    MarketingSyncUser findSyncUserByCustNumsAndAppletTime(
            @Param("apiCode") String apiCode,
            @Param("custNum") String custNum);

    List<MarketingSyncUser> getYiXinNewSyncUserByDateAndResourceChannel(
            @Param("apiCode") String apiCode,
            @Param("appletDate") String appletDate,
            @Param("resourceChannel") String resourceChannel,
            @Param("userType") String userType,
            @Param("pageSize") Integer pageSize,
            @Param("minId") Long minId);

    int updateExtend(
            @Param("apiCode") String apiCode,
            @Param("extendList") List<Map<String, String>> extendList,
            @Param("id") Long id
    );

    List<MarketingSyncUser> selectByDynamicCondition(@Param("apiCode") String apiCode, @Param("sha256Cells") List<String> sha256Cells,
                                                     @Param("whereStr") String whereStr);

    /**
     * 获取最新代运营数据
     *
     * @param apiCode  apiCode
     * @param custs    案件编号集合
     * @param cusBatch 批次号
     * @param planId   计划号
     * @return List
     */
    List<MarketingSyncUser> getSyncUserLastByCustNumsAndCusBatch(@Param("apiCode") String apiCode
            , @Param("custNums") Set<String> custs
            , @Param("cusBatch") Integer cusBatch
            , @Param("planId") Long planId);

    /**
     * @param apiCode
     * @param appletDate
     * @param userType
     * @param taskId
     * @return
     */
    List<MarketingSyncUser> getCustNumByAppletDateAndUserTypetikv_(@Param("apiCode") String apiCode,
                                                       @Param("appletDate") String appletDate,
                                                       @Param("userType") String userType,
                                                       @Param("taskId") Long taskId,
                                                       @Param("pageSize") Integer pageSize);


    int cleanUpdateById(
            @Param("apiCode") String apiCode,
            @Param("id") Long id,
            @Param("fieldItemList") List<Map<String, String>> fieldItemList
    );

    /**
     * 根据 custNum 找最新的一条cell
     * @param apiCode:
     * @param custNumSet:
     * return String
     * @author guangxiu.li
     * @date 2025/1/8
     */
    MarketingSyncUser getCellLatestByCustNum(@Param("apiCode") String apiCode, @Param("custNum") String custNum);


    List<MarketingSyncUser> getSyncUserByCells(@Param("apiCode") String apiCode
            , @Param("cellset") Set<String> custNumSet,@Param("appletDate") String appletDate,@Param("userType") String userType);

    int updateBatchData(@Param("updateSql")String update);

    List<MarketingSyncUser> getUserByCell(@Param("apiCode") String apiCode, @Param("appletDates") List<String> appletDates, @Param("cell") String cell);

    List<MarketingSyncUser> getCustNumsByCusBatchtikv_(
            @Param("apiCode") String apiCode,
            @Param("cusBatch") String cusBatch,
            @Param("minId") Long minId,
            @Param("pageSize") Integer pageSize);
    List<MarketingSyncCell> getCellByApiCodeAndMaxId(
            @Param("apiCode") String apiCode,
            @Param("maxId") Long maxId,
            @Param("pageSize") Integer pageSize);


    List<MarketingSyncUser>getUserByCustNumAndAppletData(@Param("apiCode") String apiCode, @Param("list") List<Map<String,String>> dataCondition,@Param("custNums") List<String> custNums);


    /**
     * 根据条件查询单个报告的数据量级（支持 filterCondition）
     * @param apiCode API编码
     * @param appletDate 上传日期
     * @param userType 用户类型
     * @param repushTime 更新时间
     * @param filterCondition 过滤条件
     * @return 数据量级
     */
    Integer countByCondition(@Param("apiCode") String apiCode, 
                            @Param("appletDate") String appletDate,
                            @Param("userType") String userType,
                            @Param("repushTime") String repushTime,
                            @Param("filterCondition") String filterCondition);

    List<MarketingSyncUser> getSyncUserByMD5(@Param("apiCode") String apiCode, @Param("cellMD5") List<String> cellMD5);

    /**
     * 跑分批次 Redis 过期策略：统计同步表有效数据量级（status=1 且 is_repeat in (1,2)）
     */
    long countValidSyncForScoreBatchExpire(@Param("apiCode") String apiCode);

}
