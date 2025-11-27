package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.CarClueChannelConfigDTO;
import com.br.marketing.dto.CarClueChannelDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.carclue.web.CarClueChannelService;
import com.br.marketing.vo.CarClueChannelConfigVO;
import com.br.marketing.vo.CarClueChannelVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @ClassName CarClueChannelController
 * @Description 车线索外采渠道管理
 * @Author kongbx
 * @Date 2025/5/6 10:57
 */
@RestController
@RequestMapping(value = "/carChannel")
@Tag(name = "车线索外采渠道管理", description = "车线索外采渠道管理")
public class CarClueChannelController {

    @Resource
    private CarClueChannelService carClueChannelService;

    private static final Logger log = LoggerFactory.getLogger(CarClueReportController.class);

    @PostMapping("/getCarClueChannelList")
    @Operation(summary = "车线索外采渠道管理列表", description = "车线索外采渠道管理列表")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getCarClueChannelList(@RequestBody @Valid CarClueChannelDTO request) {
        PageResultReturn result = carClueChannelService.getCarClueChannelList(request);
        if (result != null) {
            return new ApiResult<PageResultReturn>().success(result);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @Operation(summary = "判断是否存在待清洗的文档记录", description = "判断是否存在待清洗的文档记录")
    @GetMapping("/checkCleanFile")
    public ApiResult<Boolean> checkCleanFile() {
        try {
            return carClueChannelService.checkCleanFile();
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "判断是否存在待清洗的文档记录接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "更新初始外采信息", description = "更新初始外采信息")
    @PostMapping(value = "/updateInitMapping", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<Boolean> updateInitMapping(@RequestParam(value = "scope", required = false) List<String> scope,
                                                @RequestPart(value = "multipartFile", required = false) MultipartFile multipartFile) {
        try {
            return carClueChannelService.updateInitMapping(scope, multipartFile);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "判断是否存在待清洗的文档记录接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/getChannelConfig")
    @Operation(summary = "获取渠道商配置", description = "获取渠道商配置")
    @AddDataAuthBusiness
    public ApiResult<CarClueChannelConfigVO> getChannelConfig() {
        try {
            return carClueChannelService.getChannelConfig();
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "获取渠道商配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<CarClueChannelConfigVO>().fail();
        }
    }

    @Operation(summary = "新增修改渠道商配置", description = "新增修改渠道商配置")
    @PostMapping("/updateChannelConfig")
    public ApiResult<Boolean> updateChannelConfig(@RequestBody @Validated CarClueChannelConfigDTO dto) {
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            return carClueChannelService.updateChannelConfig(dto,user);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "新增修改渠道商配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }


}
