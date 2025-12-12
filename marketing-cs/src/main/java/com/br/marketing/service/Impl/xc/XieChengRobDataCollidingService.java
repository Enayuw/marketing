package com.br.marketing.service.Impl.xc;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.xiecheng.XieChengActivateDTO;
import com.br.marketing.entity.XieChengCollidingDataRob;

/**
 * 携程非周期数据撞库相关Service
 *
 * @author senyang.zheng
 * @date 2024/03/19
 */
public interface XieChengRobDataCollidingService extends DataCollidingService<XieChengCollidingDataRob> {

    void initializeTodayReleaseTime(String key);

    void resetCollidingCountAndBuildRobTask();

    /**
     * 促活数据接入后续处理
     */
    Result<Boolean> activateDataHandle(XieChengActivateDTO xieChengActivateDTO);
}
