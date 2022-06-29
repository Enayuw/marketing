package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

import java.util.HashMap;
import java.util.List;

public interface IApiToDbService {
    Result pushToDb(String apiCode);

    Result pushToDb(String apiCode, int shardingTotalCount, List<Integer> shardingItems);

    Result pushToDb(String apiCode, HashMap<String, String> params);

    Long getTaskContextId();

    /**
     *
     * @param apiCode
     * @param cusBatch 以前的客户批次号 改为现在规则id
     * @param groupType 以前的场景 改为现在规则的编号
     * @param time 规则的时间
     * @param isOnly
     * @return
     */
    Result<String> buildBatchNumber(String apiCode, String cusBatch, String groupType, String time, Integer isOnly);

    Result<Boolean> consumerUserToDb(Long synInfoId);
}
