package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

import java.util.HashMap;

public interface IApiToDbService {
    Result  pushToDb(String apiCode);

    Result  pushToDb(String apiCode,HashMap<String,String> params);

    Long getTaskContextId();

    Result<String> buildBatchNumber(String apiCode,String cusBatch,String groupType,String time,Integer isOnly);

    Result<Boolean> consumerUserToDb(Long synInfoId);
}
