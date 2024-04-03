package com.br.marketing.mapper;


import com.br.marketing.entity.CallRecord;
import org.apache.ibatis.annotations.Param;

import java.util.*;

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
}