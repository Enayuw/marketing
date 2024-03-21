package com.br.marketing.service.Impl.xc;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.XieChengCollidingDataLog;

public interface XieChengCollidingDataLogService {
    Result<Boolean> saveXieChengCollidingDataLog(XieChengCollidingDataLog xieChengCollidingDataLog);
}
