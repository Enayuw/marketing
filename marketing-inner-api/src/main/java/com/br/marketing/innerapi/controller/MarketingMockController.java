package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.mock.MockCreateCaseDTO;
import com.br.marketing.dto.mock.MockCreatePolicyDTO;
import com.br.marketing.dto.mock.MockPolicyDTO;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.MockCase;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.mock.MockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "Mock系统", tags = "Mock系统", produces = "application/json", consumes = "application/json", protocols = "http")
public class MarketingMockController {

    @Resource(name = "newMockService")
    private MockService mockService;

    @PostMapping("/getMockPolicyList")
    @ApiOperation(value = "获取Mock策略列表", notes = "分页获取获取Mock策略列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "MockQueryDTO")
    })
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

    @PostMapping("/saveOrUpdateMockPolicy")
    @ApiOperation(value = "新增或修改Mock策略列表", notes = "新增或修改Mock策略列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "request", value = "查询参数", required = true, dataType = "MockQueryDTO")
    })
    @AddDataAuthBusiness
    public ApiResult<Boolean> saveOrUpdateMockPolicy(@RequestBody @Valid MockCreatePolicyDTO dto) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            Boolean result = mockService.saveOrUpdateMockPolicy(dto,userDetail);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "新增或修改Mock策略列表接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    /**
     * 删除mock策略（支持批量删除）
     */
    @PostMapping("/deleteMockPolicies")
    @ApiOperation(value = "删除Mock策略", notes = "批量删除Mock策略")
    @ApiImplicitParam(name = "ids", value = "要删除的Mock策略名称列表", required = true, dataType = "List<Long>")
    public ApiResult<Boolean> deleteMockPolicies(@RequestBody List<Long> ids) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            Boolean result = mockService.deleteMockPolicies(ids,userDetail);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除Mock策略接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getMockCaseList")
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
     * 批量添加mock用例
     */
    @PostMapping("/batchAddMockCase")
    @ApiOperation(value = "批量添加mock用例", notes = "批量添加mock用例")
    public ApiResult<String> batchAddMockCase(@RequestBody List<MockCreateCaseDTO> list) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            return mockService.batchAddMockCase(list,userDetail);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "批量添加mock用例接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<String>().fail(ServiceResultEnum.FAILED);
        }
    }

    /**
     * 修改mock用例
     */
    @PostMapping("/updateMockCase")
    @ApiOperation(value = "修改Mock用例", notes = "修改Mock用例")
    public ApiResult<Boolean> updateMockCase(@RequestBody MockCreateCaseDTO dto) {
        try {
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            Boolean result = mockService.updateMockCase(dto,userDetail);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "修改Mock用例接口错误！错误信息：" + ex.getMessage()), ex);
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
            MarketingUserDetail userDetail = ThreadContextInfo.getUser();
            Boolean result = mockService.deleteMockCases(ids,userDetail);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "删除Mock用例接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @GetMapping("/getMockName")
    @ApiOperation(value = "查询所有的mock名称", notes = "查询所有的mock名称")
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
    @ApiOperation(value = "查询策略类型", notes = "查询策略类型")
    public ApiResult<Map<Integer, String>> getMockType() {
        try {
            return mockService.getMockType();
        }catch (Exception ex){
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "查询策略类型接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Map<Integer, String>>().fail(ServiceResultEnum.FAILED);
        }
    }

    /**
     * 批量启用/禁用Mock规则（支持批量启用）
     */
    @PostMapping("/enableMockPolicies")
    @ApiOperation(value = "批量启用/禁用Mock规则", notes = "批量启用/禁用Mock规则")
    public ApiResult<Boolean> enableMockPolicies(@RequestBody MockPolicyDTO list) {
        try {
            Boolean result = mockService.enableMockPolicies(list);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "批量启用/禁用Mock规则接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/testMockPolicy")
    @ApiOperation(value = "测试Mock规则", notes = "测试Mock规则")
    public ApiResult<String> testMockPolicy(@RequestParam(name = "mockName") String mockName) {
        try {
            return  mockService.testMockPolicy(mockName);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "测试Mock规则接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<String>().fail(ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/testNote")
    @ApiOperation(value = "测试Mock注解", notes = "测试Mock注解")
    public ApiResult<String> testNote() {
        try {
            return mockService.testNote();
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.MOCK_SERVICEERROR.getCode(),
                    "测试Mock注解接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<String>().fail(ServiceResultEnum.FAILED);
        }
    }

}
