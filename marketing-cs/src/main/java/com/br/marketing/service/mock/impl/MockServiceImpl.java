package com.br.marketing.service.mock.impl;

import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.MockPolicy;
import com.br.marketing.entity.MockPolicyExample;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.MockPolicyMapper;
import com.br.marketing.service.mock.MockService;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName MockServiceImpl
 * @Author kongbx
 * @Date 2025/6/6 16:09
 */
@Service
@Slf4j
public class MockServiceImpl implements MockService {

    @Resource
    private MockPolicyMapper mockPolicyMapper;

    @Override
    public PageResultReturn getMockPolicyList(MockQueryDTO dto, MarketingUserDetail userDetail) {
        try {
            // 执行分页查询
            PageHelper.startPage(dto.getCurrent(), dto.getSize());
            MockPolicyExample mockPolicyExample = new MockPolicyExample();

            List<MockPolicy> mockPolicies = mockPolicyMapper.selectByExample(mockPolicyExample);

            return PageResultReturn.setPageResult(mockPolicies, dto.getCurrent(), dto.getSize(), 1L);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock策略列表失败！mockName: " + dto.getMockName()), e);
            return null;
        }

    }
}
