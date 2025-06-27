package com.br.marketing.service.mock.impl;

import com.alibaba.fastjson2.JSON;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.MockCaseExample;
import com.br.marketing.entity.MockPolicy;
import com.br.marketing.entity.MockPolicyExample;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.MockCaseMapper;
import com.br.marketing.mapper.MockPolicyMapper;
import com.br.marketing.service.mock.MockService;
import com.br.marketing.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MockServiceImpl
 * @Author kongbx
 * @Date 2025/6/6 16:09
 */
@Service("newMockService")
@Slf4j
public class MockServiceImpl implements MockService {

    @Resource
    private MockPolicyMapper mockPolicyMapper;

    @Resource
    private MockCaseMapper mockCaseMapper;

    @Resource
    private RedisChgService redisChgService;

    private static final String MOCK_POLICY = "mock:policy:";

    @Override
    public PageResultReturn getMockPolicyList(MockQueryDTO dto, MarketingUserDetail userDetail) {
        try {
            // 执行分页查询
            PageHelper.startPage(dto.getCurrent(), dto.getSize());
            MockPolicyExample example = new MockPolicyExample();
            MockPolicyExample.Criteria criteria = example.createCriteria();
            if (dto.getMockName() != null && !dto.getMockName().isEmpty()) {
                criteria.andMockNameLike("%" + dto.getMockName() + "%");
            }
            if (dto.getEnabled() != null) {
                criteria.andEnabledEqualTo(dto.getEnabled());
            }
            if (dto.getUpdateTime() != null){
                criteria.andUpdateTimeGreaterThan(dto.getUpdateTime());
            }

            List<MockPolicy> mockPolicies = mockPolicyMapper.selectByExample(example);
            return PageResultReturn.setPageResult(mockPolicies, dto.getCurrent(), dto.getSize(), 1L);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock策略列表失败！mockName: " + dto.getMockName()), e);
            return null;
        }

    }

    @Override
    public List<MockCase> getMockCaseList(String mockName) {

        try {
            MockCaseExample mockCaseExample = new MockCaseExample();
            mockCaseExample.createCriteria().andMockNameEqualTo(mockName);
            return mockCaseMapper.selectByExample(mockCaseExample);
        }catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock用例列表失败！mockName: " + mockName), e);
            return null;
        }
    }

    @Override
    public Boolean addMockCase(MockCase mockCase) {
        try {
            mockCase.setCreateDate(TimeUtils.parseDateToString3return(new Date()));
            mockCase.setCreateTime(DateUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
            mockCase.setUpdateTime(DateUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
            int inserted = mockCaseMapper.insertSelective(mockCase);
            return inserted > 0;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "添加Mock用例失败！mockName: " + mockCase.getMockName()), e);
            return false;
        }
    }

    @Override
    public Boolean deleteMockCases(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        try {
            MockCaseExample example = new MockCaseExample();
            example.createCriteria().andIdIn(ids);
            int deleted = mockCaseMapper.deleteByExample(example);
            return deleted > 0;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除Mock用例失败！ids: " + ids), e);
            return false;
        }
    }

    @Override
    public Boolean saveOrUpdateMockPolicy(MockPolicy mockPolicy) {
        try {
            if (mockPolicy.getId() == null) {
                // 新增
                mockPolicy.setCreateDate(TimeUtils.parseDateToString3return(new Date()));
                mockPolicy.setCreateTime(DateUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                mockPolicy.setUpdateTime(DateUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                int inserted = mockPolicyMapper.insertSelective(mockPolicy);
                if (inserted > 0) {
                    syncPolicyToCache(mockPolicy.getId(),mockPolicy);
                }
                return inserted > 0;
            } else {
                // 更新
                mockPolicy.setUpdateTime(DateUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
                int updated = mockPolicyMapper.updateByPrimaryKeySelective(mockPolicy);
                if (updated > 0) {
                    // 更新redis
                    syncPolicyToCache(mockPolicy.getId(),mockPolicy);
                }
                return updated > 0;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "保存或更新Mock策略失败！mockName: " + mockPolicy.getMockName()), e);
            return false;
        }
    }

    @Override
    public Boolean deleteMockPolicies(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        try {
            MockPolicyExample example = new MockPolicyExample();
            example.createCriteria().andIdIn(ids);
            int deleted = mockPolicyMapper.deleteByExample(example);
            if (deleted > 0){
                for (Long id : ids){
                    removePolicyFromCache(id);
                }
            }
            return deleted > 0;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除Mock策略失败！ids: " + ids), e);
            return false;
        }
    
    }

    @Override
    public Boolean enableMockPolicies(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        try {
            MockPolicy mockPolicy = new MockPolicy();
            mockPolicy.setEnabled(0); // 0-启动
            mockPolicy.setUpdateTime(DateUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
            MockPolicyExample example = new MockPolicyExample();
            example.createCriteria().andIdIn(ids);
            int updated = mockPolicyMapper.updateByExampleSelective(mockPolicy, example);
            return updated > 0;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "启用Mock策略失败！ids: " + ids), e);
            return false;
        }
    }

    @Override
    public Boolean disableMockPolicies(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        try {
            MockPolicy mockPolicy = new MockPolicy();
            mockPolicy.setEnabled(1); // 1-关闭
            mockPolicy.setUpdateTime(DateUtils.format(new Date(),"yyyy-MM-dd HH:mm:ss"));
            MockPolicyExample example = new MockPolicyExample();
            example.createCriteria().andIdIn(ids);
            int updated = mockPolicyMapper.updateByExampleSelective(mockPolicy, example);
            return updated > 0;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "禁用Mock策略失败！ids: " + ids), e);
            return false;
        }
    }

    @Override
    public Object testMockPolicy() {
        // 这里只做简单返回，具体业务逻辑可根据实际需求补充
        return "Mock策略测试成功";
    }

    void syncPolicyToCache(long policyId,MockPolicy mockPolicy){
        // 写入redis
        String redisKey = MOCK_POLICY.concat(String.valueOf(policyId));
        String jsonObject = JSON.toJSONString(mockPolicy);
        redisChgService.set(redisKey, jsonObject);
    }

    void removePolicyFromCache(long policyId){
        // 删除redis
        String redisKey = MOCK_POLICY.concat(String.valueOf(policyId));
        redisChgService.del(redisKey);
    }
}