package com.br.marketing.service;

import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.entity.MockCase;

/**
 * @ClassName MockPolicyFactory
 * @Description mock策略加工厂
 * @Author kongbx
 * @Date 2025/6/30 17:50
 */
public interface MockPolicyFactory {

    /**
     * 策略类型
     */
    Integer policyType();

    /**
     * 策略执行
     */
    MockCase action(MockCreatePolicyDTO policy);

}
