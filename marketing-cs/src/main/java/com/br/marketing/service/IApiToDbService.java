package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

public interface IApiToDbService {
    Result  pushToDb();

    Long getTaskContextId();

    Result<String> buildBatchNumber(String apiCode,String cusBatch,String groupType,String time);

    Result<Boolean> consumerUserToDb(Long synInfoId);
}
