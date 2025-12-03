package com.br.marketing.service.mock;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.mock.MockCreateCaseDTO;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.dto.mock.MockPolicyDTO;
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

    ApiResult<MockCreatePolicyDTO> getMockDetails(Long id);

    ApiResult<Boolean> enableMockPolicies(MockPolicyDTO list);

    ApiResult<Boolean> saveOrUpdateMockPolicy(MockCreatePolicyDTO mockPolicy, MarketingUserDetail userDetail);

    ApiResult<Boolean> deleteMockPolicies(List<Long> ids, MarketingUserDetail userDetail);

    ApiResult<List<MockCase>> getMockCaseList(String mockName);

    ApiResult<List<String>> getMockName();

    ApiResult<Map<Integer,String>> getMockType();

    Result<String> queryMockConfig(String localCacheKey);

    /**
     * 提供给客户端执行策略
     * @param policy
     * @return
     */
    MockCreateCaseDTO action(MockCreatePolicyDTO policy) throws InterruptedException;

}
