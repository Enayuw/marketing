package com.br.marketing.mapper;

import com.br.marketing.entity.CallRecordLog;

public interface CallRecordLogMapper extends CallRecordLogMapperBase {
    CallRecordLog selectByrecordId(Long recordId);


}