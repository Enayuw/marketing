package com.br.marketing.service;

import com.br.marketing.dto.mock.MockCreateCaseDTO;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;

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
    MockCreateCaseDTO action(MockCreatePolicyDTO policy) throws InterruptedException;

}
