package com.br.marketing.service.strategy.pushinfolist;

import com.br.marketing.enums.TaskTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 推送信息列表查询策略工厂
 * 负责管理所有推送信息列表查询策略，并根据任务类型获取对应策略
 *
 * @author system
 * @date 2025-11-09
 */
@Slf4j
@Component
public class PushInfoListStrategyFactory {

    @Resource
    private UploadTaskPushInfoListStrategy uploadTaskPushInfoListStrategy;

    @Resource
    private ScoreTaskPushInfoListStrategy scoreTaskPushInfoListStrategy;

    /**
     * 策略映射表：任务类型 -> 策略实例
     */
    private Map<Integer, IPushInfoListStrategy> strategyMap;

    /**
     * 初始化策略映射表
     */
    @PostConstruct
    public void init() {
        strategyMap = new HashMap<>();
        strategyMap.put(TaskTypeEnum.UPLOAD_TASKS.getValue(), uploadTaskPushInfoListStrategy);
        strategyMap.put(TaskTypeEnum.SCORE_TASK.getValue(), scoreTaskPushInfoListStrategy);

        log.warn("推送信息列表查询策略工厂初始化完成，共加载 {} 个策略", strategyMap.size());
    }

    /**
     * 根据任务类型获取对应的策略
     *
     * @param taskType 任务类型（1-上传任务，2-跑分任务）
     * @return 对应的策略实例
     * @throws IllegalArgumentException 如果任务类型不存在
     */
    public IPushInfoListStrategy getStrategy(Integer taskType) {
        IPushInfoListStrategy strategy = strategyMap.get(taskType);
        if (strategy == null) {
            throw new IllegalArgumentException("未找到对应的推送信息列表查询策略，任务类型：" + taskType);
        }
        log.debug("推送信息列表查询策略获取成功：{}, 任务类型：{}",
                strategy.getClass().getSimpleName(), taskType);
        return strategy;
    }
}

