package com.br.marketing.mapper;


import com.br.marketing.entity.CallRecord;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface CallRecordMapper extends CallRecordMapperBase {
    List<CallRecord> getLastCallRecordByCustNum(@Param("custNums") Collection<String> custNums, @Param("cid") String cid);
}