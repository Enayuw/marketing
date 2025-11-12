package com.br.marketing.innerapi.controller;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.aspect.LogAnnotation;
import com.br.marketing.common.commondto.ApiNoDataResult;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.dto.mock.MockPolicyDTO;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.mock.MockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Map;

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
@Tag(name = "Mock系统", description = "Mock系统相关接口")
public class MarketingMockController {

    @Resource(name = "newMockService")
    private MockService mockService;

    @PostMapping("/getMockPolicyList")
    @Operation(summary = "获取Mock策略列表", description = "分页获取获取Mock策略列表")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getMockPolicyList(@RequestBody @Valid MockQueryDTO dto) {
        try {
            PageResultReturn result = mockService.getMockPolicyList(dto);
            return new ApiResult<PageResultReturn>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock策略列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getMockDetails")
    @Operation(summary = "查询Mock明细", description = "查询Mock明细")
    @Parameter(name = "id", description = "Mock策略ID", required = true)
    @AddDataAuthBusiness
    public ApiResult<MockCreatePolicyDTO> getMockDetails(@RequestParam("id") Long id) {
        try {
            return mockService.getMockDetails(id);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "查询Mock明细接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<MockCreatePolicyDTO>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/enableMockPolicies")
    @Operation(summary = "批量启用/禁用Mock规则", description = "批量启用/禁用Mock规则")
    public ApiResult<Boolean> enableMockPolicies(@RequestBody MockPolicyDTO list) {
        try {
            return mockService.enableMockPolicies(list);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "批量启用/禁用Mock规则接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/saveOrUpdateMockPolicy")
    @Operation(summary = "保存Mock规则", description = "保存Mock规则")
    @AddDataAuthBusiness
    public ApiResult<Boolean> saveOrUpdateMockPolicy(@RequestBody @Valid MockCreatePolicyDTO dto) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            return mockService.saveOrUpdateMockPolicy(dto, userDetail);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "保存Mock规则接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/deleteMockPolicies")
    @Operation(summary = "删除Mock策略", description = "批量删除Mock策略")
    public ApiResult<Boolean> deleteMockPolicies(@RequestBody List<Long> ids) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            return mockService.deleteMockPolicies(ids, userDetail);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除Mock策略接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getMockCaseList")
    @Operation(summary = "获取Mock策略下的所有用例", description = "获取Mock策略下的所有用例")
    @Parameter(name = "mockName", description = "mock策略名称", required = true)
    public ApiResult<List<MockCase>> getMockCaseList(@RequestParam(name = "mockName") String mockName) {
        try {
            return mockService.getMockCaseList(mockName);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "获取Mock策略列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<MockCase>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getMockName")
    @Operation(summary = "查询所有的mock名称", description = "查询所有的mock名称")
    public ApiResult<List<String>> getMockName() {
        try {
            return mockService.getMockName();
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "查询所有的mock名称接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<List<String>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getMockType")
    @Operation(summary = "查询策略类型", description = "查询策略类型")
    public ApiResult<Map<Integer, String>> getMockType() {
        try {
            return mockService.getMockType();
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "查询策略类型接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Map<Integer, String>>().fail(ServiceResultEnum.FAILED);
        }
    }

}
