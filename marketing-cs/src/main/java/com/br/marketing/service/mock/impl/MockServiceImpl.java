package com.br.marketing.service.mock.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.StringUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.mock.MockCreateCaseDTO;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.dto.mock.MockPolicyDTO;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.MockCaseExample;
import com.br.marketing.entity.MockPolicy;
import com.br.marketing.entity.MockPolicyExample;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.MockCaseMapper;
import com.br.marketing.mapper.MockPolicyMapper;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.MockPolicyFactory;
import com.br.marketing.service.mock.MockService;
import com.br.marketing.constants.MockConstants;
import com.br.marketing.service.mock.enums.MockPolicyEnum;
import com.br.marketing.util.TimeUtils;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
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

    @Resource
    private EntityOptServiceImpl entityOptService;

    @Autowired
    MockPolicyImpl mockPolicy;

    @Override
    public PageResultReturn getMockPolicyList(MockQueryDTO dto) {
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
                Date updateStartTime = Date.from(dto.getUpdateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
                Date updateEndTime = Date.from(dto.getUpdateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant());
                criteria.andUpdateTimeBetween(updateStartTime, updateEndTime);
            }

            List<MockPolicy> mockPolicies = mockPolicyMapper.selectByExample(example);
            // 统计每个MockPolicy下的MockCase数量并回填
            for (MockPolicy policy : mockPolicies) {
                MockCaseExample caseExample = new MockCaseExample();
                caseExample.createCriteria().andMockNameEqualTo(policy.getMockName()).andIsDelEqualTo(1);
                int count = mockCaseMapper.countByExample(caseExample);
                policy.setCaseCount(count);
            }
            return PageResultReturn.setPageResult(mockPolicies, dto.getCurrent(), dto.getSize());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock策略列表失败！mockName: " + dto.getMockName()), e);
            return null;
        }
    }

    @Override
    public ApiResult<MockCreatePolicyDTO> getMockDetails(Long id) {
        if (id == null) {
            return new ApiResult<MockCreatePolicyDTO>().fail("入参为空！");
        }

        MockPolicy mockPolicy = mockPolicyMapper.selectByPrimaryKey(id);
        if (mockPolicy == null) {
            return new ApiResult<MockCreatePolicyDTO>().fail("未查询到该策略！id：" + id);
        }

        String mockName = mockPolicy.getMockName();
        MockCaseExample mockCaseExample = new MockCaseExample();
        mockCaseExample.createCriteria().andMockNameEqualTo(mockName).andIsDelEqualTo(1);
        List<MockCase> mockCaseList = mockCaseMapper.selectByExample(mockCaseExample);

        MockCreatePolicyDTO mockCreatePolicyDTO = new MockCreatePolicyDTO();
        mockCreatePolicyDTO.setId(mockPolicy.getId());
        mockCreatePolicyDTO.setMockName(mockPolicy.getMockName());
        mockCreatePolicyDTO.setMockPolicyType(mockPolicy.getMockPolicyType());
        mockCreatePolicyDTO.setEnabled(mockPolicy.getEnabled());
        mockCreatePolicyDTO.setVersion(mockPolicy.getVersion());
        mockCreatePolicyDTO.setDescription(mockPolicy.getDescription());

        List<MockCreateCaseDTO> mockCreateCaseDTOS = new ArrayList<>();
        for (MockCase mockCase : mockCaseList) {
            MockCreateCaseDTO createCaseDTO = new MockCreateCaseDTO();
            createCaseDTO.setId(mockCase.getId());
            createCaseDTO.setMockName(mockCase.getMockName());
            createCaseDTO.setMockCaseName(mockCase.getMockCaseName());
            createCaseDTO.setApiCode(mockCase.getApiCode());
            createCaseDTO.setResponseBody(mockCase.getResponseBody());
            createCaseDTO.setStatusCode(mockCase.getStatusCode());
            createCaseDTO.setDelayMs(mockCase.getDelayMs());
            createCaseDTO.setDelayFluctuation(mockCase.getDelayFluctuation());
            createCaseDTO.setDescription(mockCase.getDescription());
            createCaseDTO.setEnabled(mockCase.getEnabled());
            mockCreateCaseDTOS.add(createCaseDTO);
        }
        mockCreatePolicyDTO.setMockCreateCaseDTOS(mockCreateCaseDTOS);
        return new ApiResult<MockCreatePolicyDTO>().success().setData(mockCreatePolicyDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> enableMockPolicies(MockPolicyDTO list) {
        if (list == null) {
            return new ApiResult<Boolean>().fail("入参为空！");
        }
        List<Long> ids = list.getIds();
        for (Long id : ids) {
            MockPolicy mockPolicyOld = new MockPolicy();
            try {
                mockPolicyOld = mockPolicyMapper.selectByPrimaryKey(id);
                if (mockPolicyOld == null) {
                    continue;
                }
                MockPolicy mockPolicy = new MockPolicy();
                BeanUtils.copyProperties(mockPolicyOld, mockPolicy);
                int newVersion = Integer.parseInt(mockPolicyOld.getVersion()) + 1;
                mockPolicy.setEnabled(list.getEnabled());
                mockPolicy.setUpdateTime(new Date());
                mockPolicy.setVersion(String.valueOf(newVersion));
                int updated = mockPolicyMapper.updateByPrimaryKeySelective(mockPolicy);
                if (updated <= 0) {
                    log.error("启用/禁用Mock策略失败，mockName: {}", mockPolicyOld.getMockName());
                    continue;
                }
                //增加操作日志
                entityOptService.writeOptLog(mockPolicy.getId(), mockPolicy, mockPolicyOld);
                String redisKey = RedisKeyConstant.MOCK_POLICY.concat(":").concat(mockPolicy.getMockName());
                String mockData = redisChgService.get(redisKey);
                if (StringUtils.isEmpty(mockData)) {
                    return new ApiResult<Boolean>().fail("查询redis缓存为空！mockName：" + mockPolicy.getMockName());
                }
                MockCreatePolicyDTO mockCreatePolicyDTO = JSONObject.parseObject(mockData, MockCreatePolicyDTO.class);
                mockCreatePolicyDTO.setEnabled(list.getEnabled());
                mockCreatePolicyDTO.setVersion(String.valueOf(newVersion));
                // Redis更新，失败重试3次
                redisRetry(mockCreatePolicyDTO);
            } catch (Exception e) {
                log.error("批量启用/禁用MockPolicy失败，id: {}, 错误信息: {}", id, e.getMessage(), e);
                return new ApiResult<Boolean>().fail("批量启用/禁用MockPolicy失败！mockName：" + mockPolicyOld.getMockName());
            }
        }
        return new ApiResult<Boolean>().success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> saveOrUpdateMockPolicy(MockCreatePolicyDTO dto, MarketingUserDetail userDetail) {

        if (dto == null) {
            return new ApiResult<Boolean>().fail("入参为空，请修改后重新请求！");
        }

        if (CollectionUtils.isEmpty(dto.getMockCreateCaseDTOS())) {
            return new ApiResult<Boolean>().fail("Mock用例不能为空，请修改后重新请求！");
        }

        if (!MockConstants.contains(dto.getMockName())) {
            return new ApiResult<Boolean>().fail("当前mockName不存在，请确认mockName是否正确！mockName:" + dto.getMockName());
        }

        try {
            int newVersion;
            // 新增策略
            if (dto.getId() == null) {
                MockPolicyExample mockPolicyExample = new MockPolicyExample();
                mockPolicyExample.createCriteria().andMockNameEqualTo(dto.getMockName()).andIsDelEqualTo(Constants.DATA_VALID);
                int i = mockPolicyMapper.countByExample(mockPolicyExample);
                if(i > 0){
                    return new ApiResult<Boolean>().fail("该Mock规则已存在，请检查！mockName:" + dto.getMockName());
                }
                MockPolicy mockPolicy = new MockPolicy();
                newVersion = 1;
                mockPolicy.setMockName(dto.getMockName());
                mockPolicy.setMockPolicyType(dto.getMockPolicyType());
                mockPolicy.setEnabled(dto.getEnabled());
                mockPolicy.setVersion(String.valueOf(newVersion));
                mockPolicy.setDescription(dto.getDescription());
                mockPolicy.setOptUserId(Long.valueOf(userDetail.getId()));
                mockPolicy.setOptUserName(userDetail.getUserName());
                mockPolicy.setCreateDate(TimeUtils.parseDateToString3return(new Date()));
                mockPolicy.setCreateTime(new Date());
                mockPolicy.setUpdateTime(new Date());
                mockPolicy.setIsDel(Constants.DATA_VALID);
                mockPolicyMapper.insertSelective(mockPolicy);
                // 记录操作日志
                entityOptService.writeOptLog(mockPolicy.getId(), mockPolicy, null);
            } else { // 更新
                MockPolicy mockPolicyOld = mockPolicyMapper.selectByPrimaryKey(dto.getId());

                MockPolicy mockPolicy = new MockPolicy();
                mockPolicy.setId(dto.getId());
                mockPolicy.setMockName(dto.getMockName());
                mockPolicy.setMockPolicyType(dto.getMockPolicyType());
                mockPolicy.setEnabled(dto.getEnabled());
                mockPolicy.setDescription(dto.getDescription());
                mockPolicy.setOptUserId(Long.valueOf(userDetail.getId()));
                mockPolicy.setOptUserName(userDetail.getUserName());
                mockPolicy.setUpdateTime(new Date());
                mockPolicy.setCaseCount(dto.getMockCreateCaseDTOS().size());

                newVersion = Integer.parseInt(mockPolicyOld.getVersion()) + 1;
                mockPolicy.setVersion(String.valueOf(newVersion));
                mockPolicy.setUpdateTime(new Date());
                mockPolicyMapper.updateByPrimaryKeySelective(mockPolicy);
                // 记录操作日志
                entityOptService.writeOptLog(dto.getId(), mockPolicy, mockPolicyOld);
                // 删除该Mock策略下的所有用例
                MockCaseExample mockCaseExample = new MockCaseExample();
                mockCaseExample.createCriteria().andMockNameEqualTo(dto.getMockName());
                mockCaseMapper.deleteByExample(mockCaseExample);
            }

            // 批量新增用例
            List<MockCreateCaseDTO> mockCreateCaseDTOS = dto.getMockCreateCaseDTOS();
            List<MockCase> mockCaseList = new ArrayList<>();
            for (MockCreateCaseDTO createCaseDTO : mockCreateCaseDTOS) {
                MockCase mockCase = new MockCase();
                mockCase.setMockName(createCaseDTO.getMockName());
                mockCase.setMockCaseName(createCaseDTO.getMockCaseName());
                mockCase.setApiCode(createCaseDTO.getApiCode());
                mockCase.setMockCaseName(createCaseDTO.getMockCaseName());
                mockCase.setResponseBody(createCaseDTO.getResponseBody());
                mockCase.setStatusCode(createCaseDTO.getStatusCode());
                mockCase.setDelayMs(createCaseDTO.getDelayMs());
                mockCase.setDelayFluctuation(createCaseDTO.getDelayFluctuation());
                mockCase.setDescription(createCaseDTO.getDescription());
                mockCase.setOptUserId(Long.valueOf(userDetail.getId()));
                mockCase.setOptUserName(userDetail.getUserName());
                mockCase.setEnabled(dto.getEnabled());
                mockCase.setCreateDate(TimeUtils.parseDateToString3return(new Date()));
                mockCase.setCreateTime(new Date());
                mockCase.setUpdateTime(new Date());
                mockCase.setIsDel(Constants.DATA_VALID);
                mockCaseList.add(mockCase);
            }
            mockCaseMapper.batchInsert(mockCaseList);

            // Redis更新，失败重试3次
            MockCreatePolicyDTO mockCreatePolicyDTO = new MockCreatePolicyDTO();
            mockCreatePolicyDTO.setId(dto.getId());
            mockCreatePolicyDTO.setMockName(dto.getMockName());
            mockCreatePolicyDTO.setMockPolicyType(dto.getMockPolicyType());
            mockCreatePolicyDTO.setEnabled(dto.getEnabled());
            mockCreatePolicyDTO.setVersion(String.valueOf(newVersion));
            mockCreatePolicyDTO.setDescription(dto.getDescription());
            mockCreatePolicyDTO.setMockCreateCaseDTOS(dto.getMockCreateCaseDTOS());
            redisRetry(mockCreatePolicyDTO);
        } catch (Exception e) {
            log.error("Mock保存/更新失败，mockName: {}, 错误信息: {}", dto.getMockName(), e.getMessage(), e);
            return new ApiResult<Boolean>().fail("Mock保存/更新失败，mockName: "+ dto.getMockName()+ "，错误信息: "+  e.getMessage());
        }
        return new ApiResult<Boolean>().success("添加成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> deleteMockPolicies(List<Long> ids, MarketingUserDetail userDetail) {
        if (ids == null || ids.isEmpty()) {
            return new ApiResult<Boolean>().fail("入参为空！");
        }

        for (Long id : ids) {
            try {
                MockPolicy mockPolicyOld = mockPolicyMapper.selectByPrimaryKey(id);
                mockPolicyMapper.deleteByPrimaryKey(id);
                String mockName = mockPolicyOld.getMockName();
                MockCaseExample mockCaseExample = new MockCaseExample();
                mockCaseExample.createCriteria().andMockNameEqualTo(mockName).andIsDelEqualTo(Constants.DATA_VALID);
                mockCaseMapper.deleteByExample(mockCaseExample);

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
                        try {
                            Thread.sleep(1000L * retry);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                if (!redisSuccess) {
                    return new ApiResult<Boolean>().fail("Redis删除失败，已重试3次，mockName: "+mockName+ ",错误信息:" +redisException);
                }
            } catch (Exception e) {
                return new ApiResult<Boolean>().fail("批量删除MockPolicy失败，ids:  "+ids + ",错误信息:" +e.getMessage());
            }
        }
        return new ApiResult<Boolean>().success("删除成功");
    }

    @Override
    public ApiResult<List<MockCase>> getMockCaseList(String mockName) {
        try {
            MockCaseExample mockCaseExample = new MockCaseExample();
            mockCaseExample.createCriteria().andMockNameEqualTo(mockName).andIsDelEqualTo(1);
            return new ApiResult<List<MockCase>>().success(mockCaseMapper.selectByExample(mockCaseExample));
        } catch (Exception e) {
            return new ApiResult<List<MockCase>>().fail("获取Mock用例列表失败！mockName: " + mockName);
        }
    }

    @Override
    public ApiResult<List<String>> getMockName() {
        return new ApiResult<List<String>>().success().setData(MockConstants.getAllMockNames());
    }

    @Override
    public ApiResult<Map<Integer, String>> getMockType() {
        return new ApiResult<Map<Integer, String>>().success().setData(MockPolicyEnum.getAllMockPolicyEnum());
    }

    @Override
    public Result<String> queryMockConfig(String localCacheKey) {
        try {
            String result = redisChgService.get(localCacheKey);
            if(StringUtils.isEmpty(result)){
                log.warn("mock挡板查询redis缓存为空，localCacheKey: {}", localCacheKey);
                return getMockConfig(localCacheKey);
            }
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(result);
        }catch (Exception e){
            return getMockConfig(localCacheKey);
        }
    }

    public Result<String> getMockConfig(String localCacheKey) {
        try {
            // marketing:middle:mock:policy:mockName
            String[] split = localCacheKey.split(":");
            String mockName = split[split.length - 1];

            // 若查询redis报错，则查询数据库
            MockCreatePolicyDTO mockCreatePolicyDTO = new MockCreatePolicyDTO();

            MockPolicyExample mockPolicyExample = new MockPolicyExample();
            mockPolicyExample.createCriteria().andMockNameEqualTo(mockName).andIsDelEqualTo(Constants.DATA_VALID);
            List<MockPolicy> mockPolicies = mockPolicyMapper.selectByExample(mockPolicyExample);
            // 若数据库为空 则默认未配置mock，需要返回关闭状态的挡板
            if(mockPolicies.isEmpty()){
                log.warn("mock挡板查询DB为空，localCacheKey: {}", localCacheKey);
                mockCreatePolicyDTO.setMockName(mockName);
                mockCreatePolicyDTO.setEnabled(1);
                mockCreatePolicyDTO.setVersion("0");
                mockCreatePolicyDTO.setDescription("未查询到mock配置！");
                return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(JSONObject.toJSONString(mockCreatePolicyDTO));
            }
            MockPolicy dbMockPolicy = mockPolicies.get(0);

            mockCreatePolicyDTO.setId(dbMockPolicy.getId());
            mockCreatePolicyDTO.setMockName(dbMockPolicy.getMockName());
            mockCreatePolicyDTO.setMockPolicyType(dbMockPolicy.getMockPolicyType());
            mockCreatePolicyDTO.setEnabled(dbMockPolicy.getEnabled());
            mockCreatePolicyDTO.setVersion(dbMockPolicy.getVersion());
            mockCreatePolicyDTO.setDescription(dbMockPolicy.getDescription());

            MockCaseExample mockCaseExample = new MockCaseExample();
            mockCaseExample.createCriteria().andMockNameEqualTo(mockName).andIsDelEqualTo(Constants.DATA_VALID);
            List<MockCase> mockCaseList = mockCaseMapper.selectByExample(mockCaseExample);
            List<MockCreateCaseDTO> mockCreateCaseDTOS = new ArrayList<>();
            for (MockCase mockCase : mockCaseList) {
                MockCreateCaseDTO createCaseDTO = new MockCreateCaseDTO();
                createCaseDTO.setId(mockCase.getId());
                createCaseDTO.setMockName(mockCase.getMockName());
                createCaseDTO.setMockCaseName(mockCase.getMockCaseName());
                createCaseDTO.setApiCode(mockCase.getApiCode());
                createCaseDTO.setResponseBody(mockCase.getResponseBody());
                createCaseDTO.setStatusCode(mockCase.getStatusCode());
                createCaseDTO.setDelayMs(mockCase.getDelayMs());
                createCaseDTO.setDelayFluctuation(mockCase.getDelayFluctuation());
                createCaseDTO.setDescription(mockCase.getDescription());
                createCaseDTO.setEnabled(mockCase.getEnabled());
                mockCreateCaseDTOS.add(createCaseDTO);
            }
            mockCreatePolicyDTO.setMockCreateCaseDTOS(mockCreateCaseDTOS);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(JSONObject.toJSONString(mockCreatePolicyDTO));
        }catch (Exception ex){
            log.error("mock挡板查询DB异常，localCacheKey: {}, 错误信息: {}", localCacheKey, ex.getMessage(), ex);
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("mock挡板查询DB异常");
        }
    }

    @Override
    public MockCreateCaseDTO action(MockCreatePolicyDTO policy) throws InterruptedException {
        //获取执行策略
        MockPolicyFactory mockPolicyFactory = mockPolicy.getMockPolicyFactory(policy.getMockPolicyType());
        return mockPolicyFactory.action(policy);
    }

    void syncPolicyToCache(String mockName, MockCreatePolicyDTO dto) {
        // 写入redis
        String redisKey = RedisKeyConstant.MOCK_POLICY.concat(":").concat(mockName);
        String jsonObject = JSON.toJSONString(dto);
        redisChgService.set(redisKey, jsonObject);
    }

    void removePolicyFromCache(String mockName) {
        // 删除redis
        String redisKey = RedisKeyConstant.MOCK_POLICY.concat(":").concat(mockName);
        redisChgService.del(redisKey);
    }

    void redisRetry(MockCreatePolicyDTO dto) {
        boolean redisSuccess = false;
        int retry = 0;
        Exception redisException = null;
        while (retry < 3 && !redisSuccess) {
            try {
                syncPolicyToCache(dto.getMockName(), dto);
                redisSuccess = true;
            } catch (Exception e) {
                redisException = e;
                retry++;
                log.error("Redis更新失败，准备进行第{}次重试，mockName: {}", retry, dto.getMockName(), redisException);

                try {
                    Thread.sleep(1000L * retry);
                } catch (InterruptedException ignored) {
                }
            }
        }
        if (!redisSuccess) {
            log.error("Redis更新失败，已重试3次，mockName: {}", dto.getMockName(), redisException);
            throw new RuntimeException("Redis更新失败", redisException);
        }
    }


}