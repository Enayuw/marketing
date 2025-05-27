package com.br.marketing.innerapi.controller.yunke;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.LogEncryptionCellsDto;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.yunke.DeviceTypeService;
import com.br.marketing.vo.CarClueInfoVo;
import com.br.marketing.vo.yunke.DeviceTypeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author peng.kang
 * @description: 加密手机号获取服务
 * @date 2025/5/26 18:10
 */
@RestController
@RequestMapping(value = "/cell/deviceType")
@Api(value = "机型获取服务", tags = "机型获取服务", produces = "application/json", consumes = "application/json", protocols = "http")
public class PhoneDeviceTypeController {
    private static final Logger log = LoggerFactory.getLogger(PhoneDeviceTypeController.class);
    @Resource
    private DeviceTypeService deviceTypeService;

    @PostMapping("/getEncryptionCells")
    @ApiOperation(value = "根据log手机号获取机型信息", notes = "根据log手机号获取机型信息")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = CarClueInfoVo.class)})
    @AddDataAuthBusiness
    public ApiResult<List<DeviceTypeVO>> getDeviceType(@RequestBody @Valid List<LogEncryptionCellsDto> request) {
        if (request.size() > 2000) {
            return new ApiResult<List<DeviceTypeVO>>().fail("请求量级不能超过2000");
        }
        try {
            List<DeviceTypeVO> deviceTypeVOS = deviceTypeService.getDeviceTypeByLog(request);
            return new ApiResult<List<DeviceTypeVO>>().success(deviceTypeVOS);
        } catch (Exception e) {
            log.error("根据log手机号获取机型信息接口失败", e);
            return new ApiResult<List<DeviceTypeVO>>().fail(ServiceResultEnum.FAILED);
        }

    }
}
