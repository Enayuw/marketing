package com.br.marketing.mapper;

import com.br.marketing.entity.HaierCollidingDataLog;

import java.util.List;

public interface HaierCollidingDataLogMapper extends HaierCollidingDataLogMapperBase{
    Integer saveBatchLog(List<HaierCollidingDataLog> haierCollidingDataLogs);

    int updateBySelective(HaierCollidingDataLog updateLog);
}