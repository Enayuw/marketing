package com.br.marketing.innerapi.controller;

import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueChannelConfigDTO;
import com.br.marketing.dto.CarClueChannelDTO;
import com.br.marketing.entity.CarClueManageConfig;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.carclue.web.CarClueChannelService;
import com.br.marketing.vo.CarClueChannelVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
@Api(value = "车线索外采渠道管理", tags = "车线索外采渠道管理", produces = "application/json", consumes = "application/json", protocols = "http")
public class CarClueChannelController {

    @Resource
    private CarClueChannelService carClueChannelService;

    private static final Logger log = LoggerFactory.getLogger(CarClueReportController.class);

    @PostMapping("/getCarClueChannelList")
    @ApiOperation(value = "车线索外采渠道管理列表", notes = "车线索外采渠道管理列表")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = CarClueChannelVo.class)})
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getCarClueChannelList(@RequestBody @Valid CarClueChannelDTO request) {
        PageResultReturn result = carClueChannelService.getCarClueChannelList(request);
        if (result != null) {
            return new ApiResult<PageResultReturn>().success(result);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    @ApiOperation(value = "判断是否存在待清洗的文档记录", notes = "判断是否存在待清洗的文档记录")
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

    @ApiOperation(value = "更新初始外采信息", notes = "更新初始外采信息")
    @PostMapping(value = "/updateInitMapping", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResult<Boolean> updateInitMapping(@RequestParam(value = "scope", required = false) List<String> scope,
                                                @RequestPart(value = "multipartFile", required = false) MultipartFile multipartFile) {
        try {
            return carClueChannelService.updateInitMapping(scope,multipartFile);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "判断是否存在待清洗的文档记录接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @PostMapping("/getChannelConfig")
    @ApiOperation(value = "获取渠道商配置", notes = "获取渠道商配置")
    @AddDataAuthBusiness
    public ApiResult<List<CarClueManageConfig>> getChannelConfig() {
        List<CarClueManageConfig> channelConfig = carClueChannelService.getChannelConfig();
        if (channelConfig != null) {
            return new ApiResult<List<CarClueManageConfig>>().success(channelConfig);
        }
        return new ApiResult<List<CarClueManageConfig>>().fail(ServiceResultEnum.FAILED);
    }

    @ApiOperation(value = "新增修改渠道商配置", notes = "新增修改渠道商配置")
    @PostMapping("/updateChannelConfig")
    public ApiResult<Boolean> updateChannelConfig(@RequestBody @Validated CarClueChannelConfigDTO dto) {
        try {
            return carClueChannelService.updateChannelConfig(dto);
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "新增修改渠道商配置接口错误！错误信息：" + ex.getMessage()), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }


}
