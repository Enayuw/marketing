package com.br.marketing.mapper;


import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.CustomizeUploadDataSmy;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CallRecordMapper extends CallRecordMapperBase {
    List<CallRecord> getLastCallRecordByCustNum(@Param("custNums") Collection<String> custNums, @Param("cid") String cid);

    /**
     * 2022/11/17 10:51
     * 根据案件编号+外呼开始时间判断
     *
     * @param custNumMap key custNum;value bizDate
     */
    List<CallRecord> getBlackListSettikv_(@Param("custNumMap") Map<String, String> custNumMap, @Param("apiCode") String apiCode);

    /**
     * 携程百万量级转化统计报表数据获取 外呼相关量级
     * @param cid cid
     * @param apiCode apiCode
     * @param requestData T-1
     * @param endData T
     * @param convType  convType
     * @param lineName  线路名称
     * @return Integer
     */
    Integer getCallRecordCounttikv_(@Param("cid") Long cid, @Param("apiCode") String apiCode,
                                    @Param("requestData") String requestData, @Param("endData") String endData,
                                    @Param("convType") String convType, @Param("lineName") String lineName);
    /**
     * 携程百万量级转化统计报表数据获取 外呼相关量级
     * @param cid cid
     * @param apiCode apiCode
     * @param requestData T-1
     * @param endData T
     * @param convType  convType
     * @param lineName  线路名称
     * @return Integer
     */
    Integer getOutboundCounttikv_(@Param("cid") Long cid, @Param("apiCode") String apiCode,
                                  @Param("requestData") String requestData, @Param("endData") String endData,
                                  @Param("convType") String convType, @Param("lineName") String lineName);

    List<CallRecord> getBlackListSetNewtikv_(@Param("custNumMap") Map<String, String> custNumMap, @Param("bizDate") String bizDate);

    List<String> getOneDayBlackListByCreateTime(@Param("custNumSet") Set<String> custNumSet, @Param("nowDate") String nowDate);

    Long cleanDataOfMinId(@Param("apiCode") String apiCode, @Param("date") String date);

    List<CallRecord> cleanDataByMinId(@Param("apiCode") String apiCode, @Param("date") String date,
                                                  @Param("minId") Long minId, @Param("limit") Integer limit);

    void updateSyncStatusByIds(@Param("ids") List<Long> ids, @Param("syncStatus") int syncStatus);

    void updateSyncStatusById(@Param("id") Long id, @Param("syncStatus") int syncStatus);

    /**
     * 根据user_key查询通话明细的task_name
     * @param apiCode apiCode
     * @param userKey 用户唯一编号（case_num）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return task_name，如果不存在则返回null
     */
    String queryTaskNameByUserKey(@Param("apiCode") String apiCode, 
                                   @Param("userKey") String userKey,
                                   @Param("startDate") String startDate,
                                   @Param("endDate") String endDate);

    /**
     * 批量查询task_name（打标）
     * @param apiCode apiCode
     * @param userKeyList 用户唯一编号列表（case_num）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return List<Map<String, String>>，每个Map包含case_num和task_name
     */
    List<Map<String, String>> queryTaskNameByUserKeyList(@Param("apiCode") String apiCode,
                                                           @Param("userKeyList") List<String> userKeyList,
                                                           @Param("startDate") String startDate,
                                                           @Param("endDate") String endDate);

}