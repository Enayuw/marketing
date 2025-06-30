package com.br.marketing.service.mock.impl;

import com.alibaba.fastjson2.JSON;
import com.br.common.log.AlertLog;
import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.MockCaseMapper;
import com.br.marketing.mapper.MockPolicyMapper;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.MockPolicyFactory;
import com.br.marketing.service.mock.MockService;
import com.br.marketing.service.mock.enums.MockNameEnum;
import com.br.marketing.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

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

    @Resource
    private EntityOptServiceImpl entityOptService;

    @Autowired
    MockPolicyImpl mockPolicy;

    @Override
    public PageResultReturn getMockPolicyList(MockQueryDTO dto, MarketingUserDetail userDetail) {
        try {
            // 执行分页查询
            PageHelper.startPage(dto.getCurrent(), dto.getSize());
            MockPolicyExample example = new MockPolicyExample();
            MockPolicyExample.Criteria criteria = example.createCriteria().andIsDelEqualTo(1);
            if (dto.getMockName() != null && !dto.getMockName().isEmpty()) {
                criteria.andMockNameLike("%" + dto.getMockName() + "%");
            }
            if (dto.getEnabled() != null) {
                criteria.andEnabledEqualTo(dto.getEnabled());
            }
            if (dto.getUpdateTime() != null) {
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
            mockCaseExample.createCriteria().andMockNameEqualTo(mockName).andIsDelEqualTo(1);
            return mockCaseMapper.selectByExample(mockCaseExample);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock用例列表失败！mockName: " + mockName), e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addMockCase(MockCase mockCase) {
        try {
            mockCase.setCreateDate(TimeUtils.parseDateToString3return(new Date()));
            mockCase.setCreateTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            mockCase.setUpdateTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
            int inserted = mockCaseMapper.insertSelective(mockCase);
            if (inserted <= 0) {
                throw new RuntimeException("数据库插入失败");
            } else {
                //增加操作日志
                long id = mockCase.getId();
                entityOptService.writeOptLog(id, mockCase, null);
                return true;
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "添加Mock用例失败！mockName: " + mockCase.getMockName()), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMockCases(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return false;
        }
        boolean allSuccess = true;
        try {
            for (Long id : ids) {
                MockCase mockCase = new MockCase();
                mockCase.setIsDel(9);
                MockCaseExample example = new MockCaseExample();
                example.createCriteria().andIdEqualTo(id);
                int deleted = mockCaseMapper.updateByExampleSelective(mockCase, example);
                if (deleted <= 0) {
                    allSuccess = false;
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                            "删除Mock用例失败！失败用例id:{} " + id));
                }
                //增加操作日志
                entityOptService.writeOptLog(id, mockCase, null);
            }
            return allSuccess;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除Mock用例失败！ids: " + ids), e);
            return false;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveOrUpdateMockPolicy(MockPolicy mockPolicy) {
        try {
            //判断mockName在枚举值中是否存在
            boolean isExist = MockNameEnum.getAllCodes().contains(mockPolicy.getMockName());
            if (!isExist) {
                log.warn("当前mockName不存在，请确认mockName是否正确！mockName:{}", mockPolicy.getMockName());
                return false;
            }
            //判断请求实体中是否包含id，若不包含则为新增，反之为更新
            boolean isInsert = (mockPolicy.getId() == null);
            if (isInsert) {
                // 新增
                mockPolicy.setCreateDate(TimeUtils.parseDateToString3return(new Date()));
                mockPolicy.setCreateTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                mockPolicy.setUpdateTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                mockPolicy.setVersion("1");
                mockPolicyMapper.insertSelective(mockPolicy);
                //增加日志
                Long id = mockPolicy.getId();
                entityOptService.writeOptLog(id, mockPolicy, null);
            } else {
                // 更新
                int newVersion;
                MockPolicy newMockPolicy = new MockPolicy();
                BeanUtils.copyProperties(mockPolicy, newMockPolicy);
                newVersion = Integer.parseInt(mockPolicy.getVersion()) + 1;
                newMockPolicy.setVersion(String.valueOf(newVersion));
                newMockPolicy.setUpdateTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                mockPolicyMapper.updateByPrimaryKeySelective(newMockPolicy);
                //增加操作日志
                Long id = mockPolicy.getId();
                entityOptService.writeOptLog(id, newMockPolicy, mockPolicy);
            }
            // Redis更新，失败重试3次
            redisRetry(mockPolicy);

            log.warn("MockPolicy保存/更新成功，mockName: {}, version: {}", mockPolicy.getMockName(), mockPolicy.getVersion());
        } catch (Exception e) {
            log.error("MockPolicy保存/更新失败，mockName: {}, 错误信息: {}", mockPolicy.getMockName(), e.getMessage(), e);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteMockPolicies(List<String> mockNames) {
        if (mockNames == null || mockNames.isEmpty()) {
            return false;
        }

        boolean allSuccess = true;
        for (String mockName : mockNames) {
            try {
                MockPolicyExample example = new MockPolicyExample();
                example.createCriteria().andMockNameEqualTo(mockName);
                List<MockPolicy> policies = mockPolicyMapper.selectByExample(example);
                MockPolicy mockPolicy = policies.get(0);
                int newVersion = Integer.parseInt(mockPolicy.getVersion()) + 1;
                mockPolicy.setIsDel(9);
                mockPolicy.setUpdateTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                mockPolicy.setVersion(String.valueOf(newVersion));
                mockPolicyMapper.updateByPrimaryKeySelective(mockPolicy);
                //增加操作日志
                entityOptService.writeOptLog(mockPolicy.getId(), mockPolicy, null);

                // Redis删除，失败重试3次
                boolean redisSuccess = false;
                int retry = 0;
                Exception redisException = null;
                while (retry < 3 && !redisSuccess) {
                    try {
                        removePolicyFromCache(mockName);
                        redisSuccess = true;
                    } catch (Exception e) {
                        redisException = e;
                        retry++;
                        log.error("Redis更新失败，准备进行第{}次重试，mockName: {}", retry, mockPolicy.getMockName(), redisException);

                        try {
                            Thread.sleep(1000L * retry);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                if (!redisSuccess) {
                    log.error("Redis删除失败，已重试3次，mockName: {}", mockName, redisException);
                    throw new RuntimeException("Redis删除失败", redisException);
                }
                log.warn("MockPolicy删除成功，mockName: {}, version: {}", mockName, mockPolicy.getVersion());
            } catch (Exception e) {
                log.error("批量删除MockPolicy失败，mockName: {}, 错误信息: {}", mockNames, e.getMessage(), e);
                throw e;
            }
        }
        return allSuccess;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean enableMockPolicies(List<String> mockNames) {
        if (mockNames == null || mockNames.isEmpty()) {
            return false;
        }
        boolean allSuccess = true;
        for (String mockName : mockNames) {
            try {
                MockPolicyExample example = new MockPolicyExample();
                example.createCriteria().andMockNameEqualTo(mockName);
                List<MockPolicy> policies = mockPolicyMapper.selectByExample(example);
                MockPolicy mockPolicy = policies.get(0);
                int newVersion = Integer.parseInt(mockPolicy.getVersion()) + 1;

                mockPolicy.setEnabled(0); // 启用
                mockPolicy.setUpdateTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                mockPolicy.setVersion(String.valueOf(newVersion));
                int updated = mockPolicyMapper.updateByPrimaryKeySelective(mockPolicy);
                if (updated <= 0) {
                    allSuccess = false;
                    log.error("启用Mock策略失败，mockName: {}", mockName);
                    continue;
                }
                //增加操作日志
                entityOptService.writeOptLog(mockPolicy.getId(), mockPolicy, null);

                // Redis更新，失败重试3次
                redisRetry(mockPolicy);

                log.warn("MockPolicy启用成功，mockName: {}, version: {}", mockName, mockPolicy.getVersion());
            } catch (Exception e) {
                log.error("批量启用MockPolicy失败，mockNames: {}, 错误信息: {}", mockNames, e.getMessage(), e);
            }
        }
        return allSuccess;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean disableMockPolicies(List<String> mockNames) {
        if (mockNames == null || mockNames.isEmpty()) {
            return false;
        }

        boolean allSuccess = true;

        for (String mockName : mockNames) {
            try {
                MockPolicyExample example = new MockPolicyExample();
                example.createCriteria().andMockNameEqualTo(mockName);
                List<MockPolicy> policies = mockPolicyMapper.selectByExample(example);
                MockPolicy mockPolicy = policies.get(0);
                int newVersion = Integer.parseInt(mockPolicy.getVersion()) + 1;

                mockPolicy.setEnabled(1); // 禁用
                mockPolicy.setUpdateTime(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
                mockPolicy.setVersion(String.valueOf(newVersion));
                int updated = mockPolicyMapper.updateByPrimaryKeySelective(mockPolicy);
                if (updated <= 0) {
                    allSuccess = false;
                    log.error("禁用Mock策略失败，mockName: {}", mockName);
                    continue;
                }
                //增加操作日志
                entityOptService.writeOptLog(mockPolicy.getId(), mockPolicy, null);

                // Redis更新，失败重试3次
                redisRetry(mockPolicy);

                log.warn("MockPolicy禁用成功，mockName: {}, version: {}", mockName, mockPolicy.getVersion());
            } catch (Exception e) {
                log.error("批量禁用MockPolicy失败，mockNames: {}, 错误信息: {}", mockNames, e.getMessage(), e);
            }
        }
        return allSuccess;
    }

    @Override
    public Object testMockPolicy() {
        // 这里只做简单返回，具体业务逻辑可根据实际需求补充
        return "Mock策略测试成功";
    }

    @Override
    public String getMockRedisValue(String localCacheKey) {
        return redisChgService.get(localCacheKey);
    }

    @Override
    public MockCase action(String redisValue) {
        MockPolicy policy = com.alibaba.fastjson.JSON.parseObject(redisValue, MockPolicy.class);
        //获取执行策略
        MockPolicyFactory mockPolicyFactory = mockPolicy.getMockPolicyFactory(policy.getMockPolicyType());
        if(mockPolicyFactory == null){
            return new MockCase();
        }
        return mockPolicyFactory.action(policy);
    }

    void syncPolicyToCache(String mockName, MockPolicy mockPolicy) {
        // 写入redis
        String redisKey = RedisKeyConstant.MOCK_POLICY.concat(mockName);
        String jsonObject = JSON.toJSONString(mockPolicy);
        redisChgService.set(redisKey, jsonObject);
    }

    void removePolicyFromCache(String mockName) {
        // 删除redis
        String redisKey = RedisKeyConstant.MOCK_POLICY.concat(mockName);
        redisChgService.del(redisKey);
    }

    void redisRetry(MockPolicy mockPolicy) {
        boolean redisSuccess = false;
        int retry = 0;
        Exception redisException = null;
        while (retry < 3 && !redisSuccess) {
            try {
                syncPolicyToCache(mockPolicy.getMockName(), mockPolicy);
                redisSuccess = true;
            } catch (Exception e) {
                redisException = e;
                retry++;
                log.error("Redis更新失败，准备进行第{}次重试，mockName: {}", retry, mockPolicy.getMockName(), redisException);

                try {
                    Thread.sleep(1000L * retry);
                } catch (InterruptedException ignored) {
                }
            }
        }
        if (!redisSuccess) {
            log.error("Redis更新失败，已重试3次，mockName: {}", mockPolicy.getMockName(), redisException);
            throw new RuntimeException("Redis更新失败", redisException);
        }
    }
}