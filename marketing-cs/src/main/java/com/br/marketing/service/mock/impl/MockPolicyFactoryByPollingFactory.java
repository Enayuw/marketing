package com.br.marketing.service.mock.impl;

import com.br.common.util.StringUtils;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.MockPolicy;
import com.br.marketing.service.MockPolicyFactory;
import com.br.marketing.service.mock.MockService;
import com.br.marketing.service.mock.enums.MockPolicyEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @ClassName MockPolicyFactoryByPollingFactory
 * @Description 随机
 * @Author kongbx
 * @Date 2025/6/30 17:58
 */
@Service
@Slf4j
public class MockPolicyFactoryByPollingFactory implements MockPolicyFactory {
    @Resource(name = "newMockService")
    private MockService mockService;

    @Override
    public Integer policyType() {
        return MockPolicyEnum.RANDOM.getCode();
    }

    @Override
    public MockCase action(MockPolicy policy) {
        // 参数校验
        if (policy == null || StringUtils.isBlank(policy.getMockName())) {
            log.warn("Mock策略或mockName为空");
            return new MockCase();
        }

        try {
            String mockName = policy.getMockName();

            ApiResult<List<MockCase>> apiResult = mockService.getMockCaseList(mockName);
            List<MockCase> mockCaseList = apiResult.getData();
            // 处理空列表情况
            if (CollectionUtils.isEmpty(mockCaseList)) {
                log.warn("未查询到响应mock用例，mockName：{}", mockName);
                return new MockCase();
            }

            // 随机选择一条数据（使用ThreadLocalRandom避免线程安全问题）
            int randomIndex = ThreadLocalRandom.current().nextInt(mockCaseList.size());
            MockCase mockCase = mockCaseList.get(randomIndex);

            // 处理延迟（如果需要）
            if (mockCase.getDelayMs() != null && mockCase.getDelayMs() > 0) {
                applySmartDelay(mockCase.getDelayMs());
            }

            return mockCase;

        } catch (Exception e) {
            log.error("Mock随机策略执行异常，mockName：{}", policy.getMockName(), e);
            return new MockCase();
        }
    }

    /**
     * 应用智能延迟（基础延迟 ± 随机波动）
     */
    private void applySmartDelay(long baseDelayMs) {
        // 计算波动范围（默认 ±20%）
        double fluctuation = baseDelayMs / 100.0;
        double maxDeviation = baseDelayMs * fluctuation;

        // 随机波动（-maxDeviation 到 +maxDeviation）
        long actualDelay = baseDelayMs + ThreadLocalRandom.current().nextLong((long) -maxDeviation, (long) maxDeviation);

        // 确保延迟不小于 0
        actualDelay = Math.max(0, actualDelay);

        try {
            Thread.sleep(actualDelay);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("智能延迟被中断");
        }
    }
}
