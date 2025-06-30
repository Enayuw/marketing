package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.mock.MockInitDTO;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.MockLocalCache;
import com.br.marketing.entity.MockPolicy;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.origin.CaffeineCache;
import com.br.marketing.service.mock.MockService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @ClassName MarketingMockController
 * @Description Mock系统相关接口
 * @Author kongbx
 * @Date 2025/6/6 14:55
 */
@RestController
@Configuration
@RequestMapping("/mock")
@Slf4j
@Api(value = "Mock系统", tags = "Mock系统", produces = "application/json", consumes = "application/json", protocols = "http")
public class MarketingMockController {

    @Resource(name = "newMockService")
    private MockService mockService;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private CaffeineCache caffeineCache;

    private static final String MOCK_POLICY = "mock:policy:";

    @PostConstruct
    public void init() {
        final int interval = marketingCommonConfig.getMockPollingInterval() != null && marketingCommonConfig.getMockPollingInterval() > 0
            ? marketingCommonConfig.getMockPollingInterval() 
            : 60; // 默认60秒
        
        new Thread(() -> {
            while (true) {
                try {
                    // 检查本地缓存和Redis版本一致性
                    checkAndUpdateMockCache();
                    
                    // 按照配置的间隔时间休眠
                    Thread.sleep(interval * 1000L);
                } catch (InterruptedException e) {
                    log.warn("Mock轮询线程被中断", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("Mock轮询线程执行异常", e);
                    try {
                        Thread.sleep(5000); // 异常时等待5秒后继续
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "MockCachePollingThread").start();
    }

    /**
     * 检查并更新Mock缓存
     * 比较本地缓存和Redis版本，如果不一致则更新本地缓存
     */
    private void checkAndUpdateMockCache() {
        try {
            // 创建查询条件，获取所有Mock策略
            MockQueryDTO queryDTO = new MockQueryDTO();
            queryDTO.setCurrent(1);
            queryDTO.setSize(1000);

            
            // 获取所有Mock策略的Redis key
            PageResultReturn<MockPolicy> pageResult = mockService.getMockPolicyList(queryDTO, null);
            if (pageResult == null || pageResult.getRecords() == null) {
                log.warn("获取Mock策略列表失败或为空");
                return;
            }
            
            List<MockPolicy> mockPolicies = pageResult.getRecords();
            
            for (MockPolicy policy : mockPolicies) {
                if (policy == null || policy.getMockName() == null) {
                    continue;
                }
                
                long policyId = policy.getId();
                String localCacheKey = MOCK_POLICY.concat(String.valueOf(policyId));
                String redisKey = MOCK_POLICY.concat(String.valueOf(policyId));
                
                // 获取本地缓存
                MockLocalCache localCache = caffeineCache.getMockSwitchStatus(localCacheKey);

                // 获取Redis缓存
                String redisValue = null;
                try {
                    redisValue = redisChgService.get(redisKey);
                } catch (Exception e) {
                    log.warn("获取Redis缓存失败，key: {}", redisKey, e);
                }
                //如果redis不存该key，则将db的值存到redis
                if (redisValue == null) {
                    redisValue = JSON.toJSONString(policy);
                    redisChgService.set(redisKey,redisValue);
                }

                // 比较版本
                if (localCache == null || !isVersionConsistent(localCache, redisValue)) {
                    // 版本不一致，更新本地缓存
                    updateLocalCache(localCacheKey, redisValue);
                }
            }
        } catch (Exception e) {
            log.error("检查Mock缓存版本时发生异常", e);
            throw e;
        }
    }

    /**
     * 检查版本是否一致
     */
    private boolean isVersionConsistent(MockLocalCache localCache, String redisValue) {
        MockPolicy policy = JSON.parseObject(redisValue, MockPolicy.class);
        if (localCache == null || policy == null) {
            return false;
        }

        // 比较版本号（使用ID作为版本号）
        String currentVersion = policy.getVersion();
        if (!currentVersion.equals(localCache.getVersion())) {
            return false;
        }
        
        // 比较启用状态
        if (!localCache.getEnabled().equals(policy.getEnabled())) {
            return false;
        }
        
        // 比较更新时间
        if (!localCache.getUpdateTime().equals(policy.getUpdateTime())) {
            return false;
        }

        return true;
    }

    /**
     * 更新本地缓存
     */
    private void updateLocalCache(String localCacheKey, String redisValue) {
        MockPolicy policy = JSON.parseObject(redisValue, MockPolicy.class);

        int maxRetries = 3;
        int retryCount = 0;
        
        while (retryCount < maxRetries) {
            try {
                // 创建新的本地缓存对象
                MockLocalCache newLocalCache = new MockLocalCache();
                newLocalCache.setEnabled(policy.getEnabled());
                newLocalCache.setVersion(String.valueOf(policy.getVersion()));
                newLocalCache.setUpdateTime(policy.getUpdateTime());
                
                // 更新本地缓存
                caffeineCache.storeMockSwitchStatus(localCacheKey, newLocalCache);
                
                log.info("成功更新Mock本地缓存，key: {}, version: {}", localCacheKey, newLocalCache.getVersion());
                return; // 更新成功，退出重试循环
                
            } catch (Exception e) {
                retryCount++;
                log.warn("更新Mock本地缓存失败，重试次数: {}/{}, key: {}", retryCount, maxRetries, localCacheKey, e);
                
                if (retryCount >= maxRetries) {
                    // 达到最大重试次数，删除本地缓存并抛出异常
                    caffeineCache.deleteMockSwitchStatus(localCacheKey);
                    log.error("更新Mock本地缓存失败，已达到最大重试次数，删除本地缓存，key: {}", localCacheKey);
                    throw new RuntimeException("更新Mock本地缓存失败", e);
                }
                
                // 等待一段时间后重试
                try {
                    Thread.sleep(1000 * retryCount); // 递增等待时间
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("更新Mock本地缓存时线程被中断", ie);
                }
            }
        }
    }

    @PostMapping("/getMockPolicyList")
    @ApiOperation(value = "获取Mock策略列表", notes = "分页获取获取Mock策略列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "MockQueryDTO")
    })
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getMockPolicyList(@RequestBody @Valid MockQueryDTO dto) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            PageResultReturn result = mockService.getMockPolicyList(dto, userDetail);
            return new ApiResult<PageResultReturn>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock策略列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/getMockCaseList")
    @ApiOperation(value = "获取Mock策略下的所有用例", notes = "获取Mock策略下的所有用例")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "mockName", value = "mock策略名称", paramType = "query", dataType = "string"),
    })
    public ApiResult<List<MockCase>> getMockCaseList(@RequestParam(name = "mockName") String mockName) {
        try {
            List<MockCase> mockCase = mockService.getMockCaseList(mockName);
            return new ApiResult<List<MockCase>>().success(mockCase);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock策略列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<MockCase>>().fail(ServiceResultEnum.FAILED);
        }
    }


    /**
     * 添加mock用例
     */
    @PostMapping("/addMockCase")
    @ApiOperation(value = "添加Mock用例", notes = "添加Mock用例")
    public ApiResult<Boolean> addMockCase(@RequestBody MockCase mockCase) {
        try {
            Boolean result = mockService.addMockCase(mockCase);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "添加Mock用例接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    /**
     * 删除mock用例（支持批量删除）
     */
    @PostMapping("/deleteMockCases")
    @ApiOperation(value = "删除Mock用例", notes = "批量删除Mock用例")
    @ApiImplicitParam(name = "ids", value = "要删除的Mock用例ID列表", required = true, dataType = "List<Long>")
    public ApiResult<Boolean> deleteMockCases(@RequestBody List<Long> ids) {
        try {
            Boolean result = mockService.deleteMockCases(ids);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除Mock用例接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/saveOrUpdateMockPolicy")
    @ApiOperation(value = "新增或修改Mock策略列表", notes = "新增或修改Mock策略列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "MockQueryDTO")
    })
    @AddDataAuthBusiness
    public ApiResult<Boolean> saveOrUpdateMockPolicy(@RequestBody @Valid MockPolicy mockPolicy) {
        try {
            Boolean result = mockService.saveOrUpdateMockPolicy(mockPolicy);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "新增或修改Mock策略列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }


    /**
     * 删除mock规则（支持批量删除）
     */
    @PostMapping("/deleteMockPolicies")
    @ApiOperation(value = "删除Mock规则", notes = "批量删除Mock规则")
    @ApiImplicitParam(name = "mockNames", value = "要删除的Mock规则名称列表", required = true, dataType = "List<Long>")
    public ApiResult<Boolean> deleteMockPolicies(@RequestBody List<String> mockNames) {
        try {
            Boolean result = mockService.deleteMockPolicies(mockNames);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除Mock规则接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    /**
     * 启用mock规则（支持批量启用）
     */
    @PostMapping("/enableMockPolicies")
    @ApiOperation(value = "启用Mock规则", notes = "批量启用Mock规则")
    @ApiImplicitParam(name = "mockNames", value = "要启用的Mock规则名称列表", required = true, dataType = "List<Long>")
    public ApiResult<Boolean> enableMockPolicies(@RequestBody List<String> mockNames) {
        try {
            Boolean result = mockService.enableMockPolicies(mockNames);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "启用Mock规则接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    /**
     * 禁用mock规则（支持批量禁用）
     */
    @PostMapping("/disableMockPolicies")
    @ApiOperation(value = "禁用Mock规则", notes = "批量禁用Mock规则")
    @ApiImplicitParam(name = "mockNames", value = "要禁用的Mock规则名称列表", required = true, dataType = "List<String>")
    public ApiResult<Boolean> disableMockPolicies(@RequestBody List<String> mockNames) {
        try {
            Boolean result = mockService.disableMockPolicies(mockNames);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "禁用Mock规则接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }


    /**
     * 测试mock规则
     */
    @PostMapping("/testMockPolicy")
    @ApiOperation(value = "测试Mock规则", notes = "测试Mock规则")
    public ApiResult<Object> testMockPolicy() {
        try {
            Object result = mockService.testMockPolicy();
            return new ApiResult<Object>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "测试Mock规则接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Object>().fail(ServiceResultEnum.FAILED);
        }
    }

}
