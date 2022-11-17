package com.br.marketing.mapper;


import com.br.marketing.entity.CallRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface CallRecordMapper extends CallRecordMapperBase {
    List<CallRecord> getLastCallRecordByCustNum(@Param("custNums") Collection<String> custNums, @Param("cid") String cid);

    /**
     * 2022/11/17 10:51
     * 根据案件编号+外呼开始时间判断
     */
    Set<String> getBlackListSet(@Param("custNumSet") Set<String> custNumSet
            , @Param("apiCode") String apiCode, @Param("callStartTimeStr") String callStartTimeStr);
}