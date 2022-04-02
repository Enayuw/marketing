package com.br.marketing.mapper;


import com.br.marketing.entity.CallRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface CallRecordMapper extends CallRecordMapperBase{
    List<CallRecord> getCallRecordNewByCustNum(@Param("custNums") Set<String> custNums,@Param("cid") String cid);

    List<CallRecord> getLastCallRecordByCustNum(@Param("custNums") List<String> custNums,@Param("cid") String cid);
}