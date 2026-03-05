package com.br.marketing.mapper;


import com.br.marketing.dto.autocheck.CheckUploadSyncDataDto;
import com.br.marketing.entity.*;
import com.br.marketing.vo.TodayIdTimeBySoleVo;
import com.br.marketing.vo.TransferUserVO;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface MarketingSyncInfoMapper extends MarketingSyncInfoMapperBase {
    List<String> getCusBatchByApiAndTime(@Param("apiCode") String apiCode, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    Long minSyncId(@Param("apiCode") String apiCode, @Param("cusBatch") String cusBatch, @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    List<MarketingSyncInfo> getDatalimit(@Param("apiCode") String apiCode, @Param("cusBatch") String cusBatch
            , @Param("id") Long id, @Param("beginTime") String beginTime
            , @Param("endTime") String endTime);

    List<MarketingSyncInfo> querySynInfoWithActiontikv_(
            @Param("apiCode") String apiCode,
            @Param("statusList") List<Integer> statusList,
            @Param("actionDate") String actionDate,
            @Param("createTimeStart") String createTimeStart,
            @Param("createTimeEnd") String createTimeEnd,
            @Param("cusBatch") String cusBatch,
            @Param("pageSize") Integer pageSize
    );

    void createMarketingTransferTable(@Param("tableName") String tableName);

    Integer insertBatchTransfer(@Param("execSql") String execSql);

    int insertTransfer(MarketingTransfer transfer);

    Integer selectTransfersByRequestId(@Param("apiCode") String apiCode, @Param("requestId") String requestId);

    List<MarketingSyncUser> getPreUserByTaskAndCust(@Param("apiCode") String apiCode, @Param("conditionTextByTaskIdAndCust") String conditionTextByTaskIdAndCust);

    Long countRepeat(@Param("execSql") String execSql);

    TodayIdTimeBySoleVo getSoleValidUser(@Param("execSql") String execSql);

    Integer updateRepeatUserStatus(@Param("execSql") String execSql);

    Long getMinIdByRuleScore(@Param("apiCode") String apiCode
            , @Param("sTimeStr") String sTimeStr, @Param("eTimeStr") String eTimeStr
            , @Param("whereStr") String whereStr);

    Long getMinIdByRuleScoreWithDate(@Param("apiCode") String apiCode
            , @Param("sDate") String sDate, @Param("eTimeStr") String eTimeStr
            , @Param("whereStr") String whereStr);

    Long getMinIdByRuleScoreWithValidConfig(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList, @Param("whereStr") String whereStr,
                                            @Param("validTimeStr") String validTimeStr);

    List<String> queryUserTypeListLabelWithDatetikv_(@Param("apiCode") String apiCode
            , @Param("sDate") String sDate, @Param("eTimeStr") String eTimeStr
            , @Param("whereStr") String whereStr);

    List<String> queryUserTypeListWithDatetikv_(@Param("apiCode") String apiCode
            , @Param("sDate") String sDate, @Param("eTimeStr") String eTimeStr
            , @Param("whereStr") String whereStr);

    List<String> queryUserTypeListWithValidConfigtikv_(@Param("apiCode") String apiCode
            , @Param("configList") List<MarketingDataValidConfig> configList, @Param("whereStr") String whereStr,
                                                       @Param("validTimeStr") String validTimeStr);

    Integer countByRuleScoreWithDate(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr);

    Integer countByRuleScoreWithDatetiflash_(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr);

    Integer countByRuleScoreLabelWithDate(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr, @Param("labelId") Long labelId);

    Integer countByRuleScoreLabelWithDatetiflash_(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr, @Param("labelId") Long labelId);

    Long minIdRuleScoreWithDate(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr);

    Long minIdRuleScoreWithDatetiflash_(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr);

    Long minIdRuleScoreLabelWithDate(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr, @Param("labelId") Long labelId);

    Long minIdRuleScoreLabelWithDatetiflash_(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr, @Param("labelId") Long labelId);

    List<MarketingSyncUser> selectDataRuleScoreWithDate(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr, @Param("id") Long id, @Param("pageSize") Integer pageSize,
                                                        @Param("minUnCompleteId") Long minUnCompleteId, @Param("maxId") Long maxId);

    List<MarketingSyncUser> selectDataRuleScoreWithDatetiflash_(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr, @Param("id") Long id, @Param("pageSize") Integer pageSize,
                                                                @Param("minUnCompleteId") Long minUnCompleteId, @Param("maxId") Long maxId);

    List<MarketingSyncLabelUser> selectDataRuleScoreLabelWithDate(@Param("apiCode") String apiCode, @Param("syncIdList") List<Long> syncIdList);

    List<MarketingSyncLabelUser> selectDataRuleScoreLabelWithDatetiflash_(@Param("apiCode") String apiCode, @Param("syncIdList") List<Long> syncIdList);


    Long getMaxIdByRuleScore(@Param("apiCode") String apiCode
            , @Param("sTimeStr") String sTimeStr, @Param("eTimeStr") String eTimeStr
            , @Param("whereStr") String whereStr);

    Integer countByPreUserWithRule(@Param("apiCode") String apiCode
            , @Param("sTimeStr") String sTimeStr, @Param("eTimeStr") String eTimeStr
            , @Param("whereStr") String whereStr);

    List<MarketingSyncUser> getSyncUserByRuleScore(@Param("apiCode") String apiCode
            , @Param("sTimeStr") String sTimeStr
            , @Param("eTimeStr") String eTimeStr, @Param("minId") Long minId, @Param("maxId") Long maxId, @Param("whereStr") String whereStr);

    List<MarketingSyncUser> getPreUserByInCust(@Param("apiCode") String apiCode, @Param("custs") Set<String> custs);

    /**
     * 2023-03-24 1:44
     * 废弃原因：
     * 当案件编号存在大量重复时，获取到大的对象集合，会出现内存溢出
     */
    @Deprecated
    List<MarketingSyncUser> getPreUserByInCustAndStatus(@Param("apiCode") String apiCode, @Param("custs") Set<String> custs);

    List<MarketingSyncUser> getPreUserByInCustWithNoFail(@Param("apiCode") String apiCode, @Param("custs") Set<String> custs);

    Integer countTransferFile(@Param("apiCode") String apiCode, @Param("beginTime") String beginTime
            , @Param("endTime") String endTime, @Param("groupType") String groupType, @Param("fileTypes") List<String> fileTypes);

    List<TransferUserVO> getTransferFileUser(@Param("apiCode") String apiCode, @Param("beginTime") String beginTime
            , @Param("endTime") String endTime, @Param("minId") Long minId, @Param("groupType") String groupType, @Param("fileTypes") List<String> fileTypes);

    List<TransferUserVO> getTransferFileUserByTime(@Param("apiCode") String apiCode, @Param("beginTime") String beginTime
            , @Param("endTime") String endTime, @Param("minId") Long minId);

    List<MarketingSyncUser> getSyncUserByTaskAndCust(@Param("apiCode") String apiCode, @Param("taskIds") List<String> taskIds, @Param("custNums") List<String> custNums);

    /**
     * 根据案件编号获取客户最新的场景
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @return userType
     * @author Guo Zeqiang
     * @dateTime 2022/2/11 18:17
     */
    String getUserTypeLatestByCustNum(@Param("apiCode") String apiCode, @Param("custNum") String custNum);

    /**
     * 根据cust_num获取最新数据
     *
     * @param apiCode
     * @param caseNum
     * @return
     */
    MarketingSyncUser getNewestByCusnum(@Param("apiCode") String apiCode, @Param("caseNum") String caseNum);

    /**
     * 根据cust_num获取最新数据
     *
     * @param apiCode
     * @param caseNum
     * @return
     */
    MarketingSyncUser getNewestByCusnumAndStatus(@Param("apiCode") String apiCode, @Param("caseNum") String caseNum);

    /**
     * 根据案件编号获取最新的taskId
     *
     * @param apiCode apiCode
     * @param custNum 案件编号
     * @return taskId
     * @author Guo Zeqiang
     * @dateTime 2022/2/15 10:52
     */
    String getTaskIdLatestByCustNum(@Param("apiCode") String apiCode, @Param("custNum") String custNum
            , @Param("userType") String userType);

    /**
     * 获取案件编号的上传时间
     *
     * @param apiCode  apiCode
     * @param custNum  案件编号
     * @param userType 场景
     * @return AppletTime
     * @author Guo Zeqiang
     * @dateTime 2022/2/15 10:52
     */
    String getAppletTimeByCustNumAndUserType(@Param("apiCode") String apiCode
            , @Param("custNum") String custNum, @Param("userType") String userType);

    /**
     * 获取案件编号的落库的创建时间
     *
     * @param apiCode  apiCode
     * @param custNum  案件编号
     * @param userType 场景
     * @return AppletTime
     * @author Guo Zeqiang
     * @dateTime 2022/2/18 10:52
     */
    Date getCreatTimeByCustNumAndUserType(@Param("apiCode") String apiCode
            , @Param("custNum") String custNum, @Param("userType") String userType);

    /**
     * 获取cust_num notLike upload的最新数据的taskId
     *
     * @param apiCode
     * @param custNum
     * @return
     */
    String getTaskIdByCustNumNotLikeUpload(@Param("apiCode") String apiCode, @Param("custNum") String custNum);


    MarketingSyncUser getMarketingSyncMaxIdByAppletDate(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate);

    MarketingSyncUser getMarketingSyncMinIdByAppletDate(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate);

    List<MarketingSyncUser> getByMaxIdAndMinId(@Param("apiCode") String apiCode, @Param("beginId") Long beginId, @Param("endIdLe") Long endIdLe);


    MarketingSyncUser getCellFromCurrent(@Param("apiCode") String apiCode, @Param("custNum") String custNum);


    int updateBySyncHaLuo(@Param("marketingSync") MarketingSyncUser marketingSyncUser,
                          @Param("apiCode") String apiCode,
                          @Param("id") Long id);

    int updateBySyncHaLuoRemark(@Param("marketingSync") MarketingSyncUser marketingSyncUser,
                                @Param("apiCode") String apiCode,
                                @Param("id") Long id);

    int selectCountError(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate);

    /**
     * 根据cell获取上传接口最新一条数据
     *
     * @param apiCode
     * @param cell
     * @return
     */
    MarketingSyncUser getNewestPreUserByCell(@Param("apiCode") String apiCode, @Param("cell") String cell);

    /**
     * 根据cell获取上传接口最新一条数据
     *
     * @param apiCode
     * @param cell
     * @return
     */
    MarketingSyncUser getNewestPreUserByCellAndStatus(@Param("apiCode") String apiCode, @Param("cell") String cell);


    List<MarketingSyncUser> getCustNumAppletDateByCustNumStart(@Param("apiCode") String apiCode
            , @Param("custNums") Set<String> custNums, @Param("startDate") String startDate);

    List<MarketingSyncUser> getVaildUserByRequestId(@Param("apiCode") String apiCode, @Param("requestId") String requestId);


    /**
     *  根据apiCode 和 groupType 查询
     * @param apiCode
     * @param groupType
     * @return
     */
    List<MarketingSyncUser> getSmyDataByGroupType(@Param("apiCode") String apiCode,@Param("groupType") String groupType);

    int getUnresolvedCount(@Param("apiCode") String apiCode, @Param("startDate") String startDate,@Param("endDate") String endDate);

    /**
     * 查询
     * @param cusBatch cusBatch
     * @param apiCode apiCode
     * @return java.lang.String 查询 applet_date 结果
     */
    String getAppletDateByCusBatch(@Param("cusBatch") String cusBatch,@Param("apiCode") String apiCode);

    /**
     * 查询
     * @param appletDate appletDate
     * @param apiCode apiCode
     * @return java.util.List<java.lang.String> 查询到的cus_batch集合
     */
    List<String> getCusBatchByAppletDate(@Param("appletDate") String appletDate,@Param("apiCode") String apiCode);

    List<MarketingSyncUser> getDataByIdList(@Param("apiCode") String apiCode, @Param("idList") List<Long> idList);

    MarketingSyncInfo getMarketingSyncInfoByDate(@Param("apiCode") String apiCode, @Param("appletDate") String appletDate, @Param("actualNum") Integer actualNum);

    List<MarketingSyncUser> getMarketingSyncInfoByRequestBatch(@Param("apiCode") String apiCode, @Param("requestBatch") String requestBatch);

    /**
     * 根据apiCode和requestBatch查询MarketingSyncInfo
     * @param apiCode
     * @param requestBatch
     * @return
     */
    MarketingSyncInfo getByApiCodeAndRequestBatch(@Param("apiCode") String apiCode, @Param("requestBatch") String requestBatch);

    List<MarketingSyncUser> getMarketingSyncByCondition(@Param("apiCode") String apiCode, @Param("operateType") String operateType,
                                                        @Param("appletDate") String appletDate, @Param("userType") String userType,
                                                        @Param("createTime") Date createTime, @Param("filterCondition") String filterCondition,
                                                        @Param("minId") Long minId);


    List<String> getBatchNameByUsertikv_(@Param("apiCode") String apiCode,@Param("syncReportList") List<MarketingSyncReport> syncReportList,
                                                              @Param("filterCondition") String filterCondition);

    /**
     * 自动化巡检：上传场景 - 前一天08:00快照（单表）。
     *
     * <p>说明：当分表不存在时，MySQL 会抛错（1146）。上层按需 try/catch 处理。</p>
     */
    CheckUploadSyncDataDto getLastDay8DataByApiCode(@Param("apiCode") String apiCode);

    /**
     * 自动化巡检：上传场景 - 当天最新快照（单表）。
     *
     * <p>说明：当分表不存在时，MySQL 会抛错（1146）。上层按需 try/catch 处理。</p>
     */
    CheckUploadSyncDataDto getLatestDataByApiCode(@Param("apiCode") String apiCode);
}