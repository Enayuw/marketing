package com.br.marketing.service.mock;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.mock.MockCreateCaseDTO;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.auth.MarketingUserDetail;

import java.util.List;
import java.util.Map;

/**
 * @ClassName MockService
 * @Author kongbx
 * @Date 2025/6/6 16:01
 */
public interface MockService {

    PageResultReturn getMockPolicyList(MockQueryDTO dto);

    Boolean saveOrUpdateMockPolicy(MockCreatePolicyDTO mockPolicy, MarketingUserDetail userDetail);

    Boolean deleteMockPolicies(List<Long> ids, MarketingUserDetail userDetail);

    List<MockCase> getMockCaseList(String mockName);

    ApiResult<String> batchAddMockCase(List<MockCreateCaseDTO> list, MarketingUserDetail userDetail);

    Boolean updateMockCase(MockCreateCaseDTO mockCase, MarketingUserDetail userDetail);

    Boolean deleteMockCases(List<Long> ids, MarketingUserDetail userDetail);


    ApiResult<String> testMockPolicy(String mockName);

    ApiResult<String> testNote();

    /**
     * 提供给客户端获取redis缓存
     * @param localCacheKey
     * @return
     */
    String getMockRedisValue(String localCacheKey);

    /**
     * 提供给客户端执行策略
     * @param redisValue
     * @return
     */
    MockCase action(String redisValue);


    ApiResult<List<String>> getMockName();

    ApiResult<Map<Integer,String>> getMockType();
}
