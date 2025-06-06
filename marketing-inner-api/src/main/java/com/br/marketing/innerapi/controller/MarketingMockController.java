package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.mock.MockQueryDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.mock.MockService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

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

    @Resource
    private MockService mockService;

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

}
