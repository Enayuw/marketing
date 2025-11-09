package com.br.marketing.service.strategy.pushpreview;

import com.br.marketing.dto.PushCustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 推送预览策略工厂
 * 负责管理所有推送预览策略，并根据请求选择合适的策略
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
     * 策略列表，按优先级排序
     */
    private List<IPushPreviewStrategy> strategies;

    /**
     * 初始化策略列表
     */
    @PostConstruct
    public void init() {
        strategies = new ArrayList<>();
        strategies.add(uploadTaskPushPreviewStrategy);
        strategies.add(xieChengScorePushPreviewStrategy);
        strategies.add(mergeScorePushPreviewStrategy);
        strategies.add(commonScorePushPreviewStrategy);

        // 按优先级排序，优先级值越小越靠前
        strategies.sort(Comparator.comparingInt(IPushPreviewStrategy::priority));

        log.info("推送预览策略工厂初始化完成，共加载 {} 个策略", strategies.size());
    }

    /**
     * 根据DTO选择合适的策略
     * 策略匹配顺序：
     * 1. 上传任务 (优先级1)
     * 2. 携程跑分任务 (优先级2)
     * 3. 合并跑分任务 (优先级3)
     * 4. 通用跑分任务 (优先级4，兜底)
     *
     * @param dto 推送客户DTO
     * @return 匹配的策略
     * @throws IllegalArgumentException 如果没有找到合适的策略
     */
    public IPushPreviewStrategy getStrategy(PushCustomerDTO dto) {
        for (IPushPreviewStrategy strategy : strategies) {
            if (strategy.support(dto)) {
                log.warn("推送预览策略匹配成功：{}, 任务类型：{}, apiCode：{}",
                        strategy.getClass().getSimpleName(), 
                        dto.getTaskType(), 
                        dto.getApiCode());
                return strategy;
            }
        }
        throw new IllegalArgumentException("未找到合适的推送预览策略");
    }
}

