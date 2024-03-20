package com.br.marketing.service.Impl.xc;

import io.swagger.models.auth.In;

/**
 * 携程撞库数据清洗接口
 */

public interface XieChengCollidingDataCleanService {

    void process(String tableName,
                 String filterScore,
                 String packageName,
                 Boolean loopCycle,
                 Boolean loopCycleNon,
                 Integer priority,
                 String collidingTime,
                 Integer ruleTypeFlag);

}
