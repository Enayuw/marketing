package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;

public interface IScoreResultService {

    Result<String> isFilterScoreByTransfer(String apiCode, String ruleLabel);

    Result<String> filterScoreResByTransfer(String apiCode, String custNum, String content);
}
