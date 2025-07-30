package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycle;

import java.util.List;

/**
 * 携程CPS周期撞库数据服务接口
 * @Author chenh
 * @Date 2025-06-26
 */
public interface XieChengCpsLoopCycleDataService extends DataCollidingService<XieChengCpsCollidingDataLoopCycle> {
    
    /**
     * CPS周期数据撞库处理
     */
    void process();
}