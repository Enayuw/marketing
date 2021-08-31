package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.SoleRuleConfig;

public interface IScoreConfigService {
    Result<SoleRuleConfig> getSoleConfig(String apiCode);
}
