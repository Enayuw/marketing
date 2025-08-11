package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.XieChengCpsCollidingDataLoopCycle;
import com.br.marketing.entity.XieChengCpsCollidingDataRob;

import java.util.List;

/**
 * 携程CPS非周期数据撞库服务接口
 * @Author chenh
 * @Date 2025-06-26
 */
public interface XieChengCpsRobDataCollidingService extends DataCollidingService<XieChengCpsCollidingDataRob> {
    /**
     * 处理非周期撞库数据
     * 查询条件：retryCount=0，push_time=null
     */
    void process();
} 