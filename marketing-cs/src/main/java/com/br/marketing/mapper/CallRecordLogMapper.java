package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecord;
import com.br.marketing.entity.CallRecordLog;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CallRecordLogMapper extends CallRecordLogMapperBase {
    CallRecordLog selectByrecordId(Long recordId);

    /**
     * 批量插入 b_car_clue_info 数据
     *
     * @param callRecords 待插入的数据列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("callRecords") List<CallRecord> callRecords);
}