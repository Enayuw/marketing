package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.XieChengCollidingDataLoopCycle;

public interface XcLoopCycleDataService extends DataCollidingService<XieChengCollidingDataLoopCycle>{
    /**
     * TRUE数据撞库方法
     */
    void process();

    /**
     * 是否开启撞库
     * @return
     */
    boolean canStart();
}
