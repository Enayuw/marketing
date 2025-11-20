package com.br.marketing.service.strategy.pushcustomer;

import com.br.marketing.service.strategy.pushpreview.PushPreviewStrategyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 推送客户策略工厂
 * 负责管理所有推送客户策略，并根据策略类型枚举获取对应策略
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class PushCustomerStrategyFactory {

    @Resource
    private UploadTaskPushCustomerStrategy uploadTaskPushCustomerStrategy;

    @Resource
    private XieChengScorePushCustomerStrategy xieChengScorePushCustomerStrategy;

    @Resource
    private MergeScorePushCustomerStrategy mergeScorePushCustomerStrategy;

    @Resource
    private CommonScorePushCustomerStrategy commonScorePushCustomerStrategy;

    /**
     * 策略映射表：枚举 -> 策略实例
     */
    private Map<PushPreviewStrategyEnum, IPushCustomerStrategy> strategyMap;

    /**
     * 初始化策略映射表
     */
    @PostConstruct
    public void init() {
        strategyMap = new HashMap<>();
        strategyMap.put(PushPreviewStrategyEnum.UPLOAD_TASK, uploadTaskPushCustomerStrategy);
        strategyMap.put(PushPreviewStrategyEnum.XIE_CHENG_SCORE, xieChengScorePushCustomerStrategy);
        strategyMap.put(PushPreviewStrategyEnum.MERGE_SCORE, mergeScorePushCustomerStrategy);
        strategyMap.put(PushPreviewStrategyEnum.COMMON_SCORE, commonScorePushCustomerStrategy);

        log.warn("推送客户策略工厂初始化完成，共加载 {} 个策略", strategyMap.size());
    }

    /**
     * 根据策略类型枚举获取对应的策略
     *
     * @param strategyType 策略类型枚举
     * @return 对应的策略实例
     * @throws IllegalArgumentException 如果策略类型不存在
     */
    public IPushCustomerStrategy getStrategy(PushPreviewStrategyEnum strategyType) {
        IPushCustomerStrategy strategy = strategyMap.get(strategyType);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到对应的推送客户策略：" + strategyType);
        }
        log.warn("推送客户策略获取成功：{}, 策略类型：{}",
                strategy.getClass().getSimpleName(),
                strategyType.getDesc());
        return strategy;
    }
}

