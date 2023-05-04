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
}