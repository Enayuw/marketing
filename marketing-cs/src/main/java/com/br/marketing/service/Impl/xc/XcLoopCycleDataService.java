package com.br.marketing.service.Impl.xc;

import com.br.marketing.entity.XieChengCollidingDataLoopCycle;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface XcLoopCycleDataService extends DataCollidingService<XieChengCollidingDataLoopCycle>{
//    void pushDataAndHandleResult(List<XieChengCollidingDataLoopCycle> list, AtomicInteger failNum);
    void process();
}
