package com.br.marketing.service.Impl.xc;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.br.marketing.entity.XieChengCollidingDataRob;

/**
 * 携程非周期数据撞库相关Service
 *
 * @author senyang.zheng
 * @date 2024/03/19
 */
public interface XieChengRobDataCollidingService extends DataCollidingService<XieChengCollidingDataRob>{

    void collidingData(List<String> packageIds);

//    void pushRobCollidingData(List<XieChengCollidingDataRob> robData, AtomicInteger failNum);
}
