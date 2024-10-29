package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.XieChengCollidingDataLoopCycle;

import java.util.List;

public interface XcLoopCycleDataService extends DataCollidingService<XieChengCollidingDataLoopCycle>{
    /**
     * TRUE数据撞库方法
     */
    void process();

    /**
     * 是否开启撞库
     */
    boolean canStart();

    /**
     * 剔除转化数据convType=107或105
     */
    List<String> excludeData(List<String> cells, String dataSourceType);
}
