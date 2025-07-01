package com.br.marketing.service.mock;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.MockPolicy;
import com.br.marketing.entity.auth.MarketingUserDetail;

import java.util.List;

/**
 * @ClassName MockService
 * @Author kongbx
 * @Date 2025/6/6 16:01
 */
public interface MockService {

    PageResultReturn getMockPolicyList(MockQueryDTO dto, MarketingUserDetail userDetail);

    List<MockCase> getMockCaseList(String mockName);

    /**
     * 添加Mock用例
     */
    Boolean addMockCase(com.br.marketing.entity.MockCase mockCase);

    /**
     * 批量删除Mock用例
     */
    Boolean deleteMockCases(java.util.List<Long> ids);

    Boolean saveOrUpdateMockPolicy(MockPolicy mockPolicy);

    Boolean deleteMockPolicies(List<String> mockNames);

    Boolean enableMockPolicies(List<String> mockNames);

    Boolean disableMockPolicies(List<String> mockNames);

    ApiResult<String> testMockPolicy(String mockName);

    String getMockRedisValue(String localCacheKey);

    MockCase action(String redisValue);
}
