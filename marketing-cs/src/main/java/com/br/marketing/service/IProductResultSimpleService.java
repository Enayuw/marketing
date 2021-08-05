package com.br.marketing.service;


import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.vo.ConfigByApiCodeVO;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IProductResultSimpleService {
    Result buildResult(JSONObject hxJson, Set<String> products, StringBuilder sb,
                       Map<String, String> proFieldMap, String sep, String apiCode,String strategyId,JSONObject esResult);

    Result<String> getFieldsStrInfo(String apiCode,String strategyId);

    Result<List<String>> getFieldsInfo(String apiCode,String strategyId);

    Result<String> getBaseHeadInfo(String apiCode, String groupType);

    Result<String> getBaseHeadInfoByTaskId(Long taskId);

    Result<List<String>> getFlagProduct();

    Result<ConfigByApiCodeVO> getConfigByApiCode(String apiCode);
}
