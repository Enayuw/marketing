package com.br.marketing.service.mock.impl;

import com.br.marketing.dto.mock.MockCreateCaseDTO;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.service.MockPolicyFactory;
import com.br.marketing.service.mock.enums.MockPolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @ClassName MockPolicyFactoryByPollingFactory
 * @Description Mock随机策略实现 - 随机选择测试用例并支持智能延迟
 * @Author kongbx
 * @Date 2025/6/30 17:58
 */
@Service
@Slf4j
public class MockPolicyFactoryByPollingFactory implements MockPolicyFactory {

    /** 最大延迟时间限制（毫秒）- 防止过长延迟影响系统性能 30秒*/
    private static final int MAX_DELAY_MS = 30_000;

    /** 最大波动百分比限制 - 防止波动过大 100% */
    private static final int MAX_FLUCTUATION_PERCENT = 100;


    @Override
    public Integer policyType() {
        return MockPolicyEnum.RANDOM.getCode();
    }

    @Override
    public MockCreateCaseDTO action(MockCreatePolicyDTO policy) throws InterruptedException {

        String mockName = policy.getMockName();
        log.warn("【Mock随机策略】开始执行，mockName: {}", mockName);

        try {
            List<MockCreateCaseDTO> mockCreateCaseDTOS = policy.getMockCreateCaseDTOS();
            // 处理空列表情况
            if (CollectionUtils.isEmpty(mockCreateCaseDTOS)) {
                log.warn("【Mock随机策略】未查询到mock用例，mockName: {}", mockName);
                return null;
            }
            // 随机选择一条数据（使用ThreadLocalRandom避免线程安全问题）
            int randomIndex = ThreadLocalRandom.current().nextInt(mockCreateCaseDTOS.size());
            MockCreateCaseDTO mockCase = mockCreateCaseDTOS.get(randomIndex);
            log.warn("【Mock随机策略】随机选择用例: {} (索引: {}), mockName: {}", 
                    mockCase.getMockCaseName(), randomIndex, mockName);

            // 应用智能延迟
            applyIntelligentDelay(mockCase, mockName);
            return mockCase;
        } catch (Exception e) {
            log.error("【Mock随机策略】执行异常，mockName: {}", mockName, e);
            throw e;
        }
    }

    /**
     * 应用智能延迟
     * 支持基础延迟 + 随机波动（±百分比）
     * 
     * @param mockCase Mock用例
     * @param mockName Mock名称（用于日志）
     * @throws InterruptedException 延迟被中断时抛出
     */
    private void applyIntelligentDelay(MockCreateCaseDTO mockCase, String mockName) throws InterruptedException {
        Integer baseDelayMs = mockCase.getDelayMs();
        Integer delayFluctuation = mockCase.getDelayFluctuation();
        
        // 参数校验
        if (baseDelayMs == null || baseDelayMs <= 0) {
            log.warn("【Mock智能延迟】无需延迟，baseDelayMs: {}, mockName: {}", baseDelayMs, mockName);
            return;
        }
        
        // 延迟时间安全检查
        if (baseDelayMs > MAX_DELAY_MS) {
            log.warn("【Mock智能延迟】延迟时间超过最大限制，调整为最大值。原始: {}ms, 调整为: {}ms, mockName: {}", 
                    baseDelayMs, MAX_DELAY_MS, mockName);
            baseDelayMs = MAX_DELAY_MS;
        }
        
        // 计算最终延迟时间
        int finalDelayMs = calculateFinalDelay(baseDelayMs, delayFluctuation, mockName);
        
        // 执行延迟
        if (finalDelayMs > 0) {
            log.warn("【Mock智能延迟】开始延迟 {}ms, mockName: {}", finalDelayMs, mockName);
            long startTime = System.currentTimeMillis();
            
            Thread.sleep(finalDelayMs);
            
            long actualDelayMs = System.currentTimeMillis() - startTime;
            log.warn("【Mock智能延迟】延迟完成，预期: {}ms, 实际: {}ms, mockName: {}", 
                    finalDelayMs, actualDelayMs, mockName);
        }
    }
    
    /**
     * 计算最终延迟时间（包含随机波动）
     * 
     * @param baseDelayMs 基础延迟时间（毫秒）
     * @param delayFluctuation 延迟波动百分比（可为null）
     * @param mockName Mock名称（用于日志）
     * @return 最终延迟时间（毫秒）
     */
    private int calculateFinalDelay(int baseDelayMs, Integer delayFluctuation, String mockName) {
        // 无波动配置，直接返回基础延迟
        if (delayFluctuation == null || delayFluctuation <= 0) {
            log.warn("【Mock延迟计算】无波动配置，使用基础延迟: {}ms, mockName: {}", baseDelayMs, mockName);
            return baseDelayMs;
        }
        
        // 波动百分比安全检查
        if (delayFluctuation > MAX_FLUCTUATION_PERCENT) {
            log.warn("【Mock延迟计算】波动百分比超过最大限制，调整为最大值。原始: {}%, 调整为: {}%, mockName: {}", 
                    delayFluctuation, MAX_FLUCTUATION_PERCENT, mockName);
            delayFluctuation = MAX_FLUCTUATION_PERCENT;
        }
        
        // 计算波动范围：基础延迟 ± (基础延迟 * 波动百分比 / 100)
        int fluctuationRange = (int) (baseDelayMs * delayFluctuation / 100.0);
        
        // 生成随机波动：[-fluctuationRange, +fluctuationRange]
        int randomFluctuation = ThreadLocalRandom.current().nextInt(-fluctuationRange, fluctuationRange + 1);
        
        // 计算最终延迟时间
        int finalDelayMs = baseDelayMs + randomFluctuation;
        
        // 确保最终延迟时间不为负数
        finalDelayMs = Math.max(0, finalDelayMs);
        
        log.warn("【Mock延迟计算】基础延迟: {}ms, 波动: ±{}ms ({}%), 随机波动: {}ms, 最终延迟: {}ms, mockName: {}", 
                baseDelayMs, fluctuationRange, delayFluctuation, randomFluctuation, finalDelayMs, mockName);
        
        return finalDelayMs;
    }
}
