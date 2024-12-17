package com.br.marketing.service;


import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.vo.BaseHead;
import com.br.marketing.vo.BaseHeadConfigVO;
import com.br.marketing.vo.ConfigByApiCodeVO;

import java.util.List;

public interface IProductResultSimpleService {
    Result buildResult(JSONObject hxJson, StringBuilder sb, String sep, MarketingUser user,JSONObject esResult);

    Result<String> getFieldsStrInfo(String apiCode,String batchNumber,String sep);

    Result<List<String>> getFieldsInfo(String apiCode,String batchNumber);

    String getStrategyProductConfigStr(String apiCode,String batchNumber);

    BaseHeadConfigVO getOrderBaseHeadInfo(BaseHeadConfigVO vo);

    Result<String> getBaseHeadInfo(String apiCode, String groupType);

    Result<String> getBaseHeadInfoByTaskId(Long taskId);

    Result<String> getCurrentBaseHeadInfoByTaskId(Long taskId ,String sep);

    Result<BaseHeadConfigVO> getBaseHeadConfig(String apiCode, String groupType);

    Result<List<String>> getFlagProduct();

    Result<String> getFlagProductStr();

    Result<String> updateFlagProduct(String productStr);

    Result<ConfigByApiCodeVO> getConfigByApiCode(String apiCode);

    void  initHead(StringBuilder head, String sep, MarketingTask task);

    void offLineHeadComplete(List<String> showHeads, List<BaseHead> heads);
}
