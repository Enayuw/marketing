package com.br.marketing.service.strategy.pushpreview;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 推送预览策略工厂
 * 负责管理所有推送预览策略，并根据策略类型枚举获取对应策略
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class PushPreviewStrategyFactory {

    @Resource
    private UploadTaskPushPreviewStrategy uploadTaskPushPreviewStrategy;

    @Resource
    private XieChengScorePushPreviewStrategy xieChengScorePushPreviewStrategy;

    @Resource
    private MergeScorePushPreviewStrategy mergeScorePushPreviewStrategy;

    @Resource
    private CommonScorePushPreviewStrategy commonScorePushPreviewStrategy;

    /**
     * 策略映射表：枚举 -> 策略实例
     */
    private Map<PushPreviewStrategyEnum, IPushPreviewStrategy> strategyMap;

    /**
     * 初始化策略映射表
     */
    @PostConstruct
    public void init() {
        strategyMap = new HashMap<>();
        strategyMap.put(PushPreviewStrategyEnum.UPLOAD_TASK, uploadTaskPushPreviewStrategy);
        strategyMap.put(PushPreviewStrategyEnum.XIE_CHENG_SCORE, xieChengScorePushPreviewStrategy);
        strategyMap.put(PushPreviewStrategyEnum.MERGE_SCORE, mergeScorePushPreviewStrategy);
        strategyMap.put(PushPreviewStrategyEnum.COMMON_SCORE, commonScorePushPreviewStrategy);

        log.warn("推送预览策略工厂初始化完成，共加载 {} 个策略", strategyMap.size());
    }

    /**
     * 根据策略类型枚举获取对应的策略
     *
     * @param strategyType 策略类型枚举
     * @return 对应的策略实例
     * @throws IllegalArgumentException 如果策略类型不存在
     */
    public IPushPreviewStrategy getStrategy(PushPreviewStrategyEnum strategyType) {
        IPushPreviewStrategy strategy = strategyMap.get(strategyType);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到对应的推送预览策略：" + strategyType);
        }
        log.warn("推送预览策略获取成功：{}, 策略类型：{}", 
                strategy.getClass().getSimpleName(), 
                strategyType.getDesc());
        return strategy;
    }
}

