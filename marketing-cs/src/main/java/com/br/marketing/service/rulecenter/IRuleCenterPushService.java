package com.br.marketing.service.rulecenter;

import com.br.marketing.common.commondto.Result;

public interface IRuleCenterPushService {
    Result<Boolean> pushData(Long id);
}
