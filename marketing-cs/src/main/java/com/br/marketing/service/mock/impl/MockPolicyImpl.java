package com.br.marketing.service.mock.impl;

import com.br.marketing.service.MockPolicyFactory;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName MockPolicyImpl
 * @Author kongbx
 * @Date 2025/6/30 18:12
 */
@Service
public class MockPolicyImpl {
    @Autowired
    ApplicationContext applicationContext;

    Map<Integer, List<MockPolicyFactory>> map;

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @PostConstruct
    void init() {
        Map<String, MockPolicyFactory> beans = applicationContext.getBeansOfType(MockPolicyFactory.class);
        this.map = beans.values().stream().collect(Collectors.groupingBy(t -> t.policyType()));
    }

    public MockPolicyFactory getMockPolicyFactory(Integer type) {
        List<MockPolicyFactory> mockPolicyFactories = this.map.get(type);
        if (mockPolicyFactories.size() <= 0) {
            return null;
        }
        return mockPolicyFactories.get(0);
    }
}
