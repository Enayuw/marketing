package com.br.marketing.innerapi.controller.yunke;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.LogEncryptionCellsDto;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.yunke.DeviceTypeService;
import com.br.marketing.vo.CarClueInfoVo;
import com.br.marketing.vo.yunke.DeviceTypeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "机型获取服务", description = "机型获取服务")
public class PhoneDeviceTypeController {
    private static final Logger log = LoggerFactory.getLogger(PhoneDeviceTypeController.class);
    @Resource
    private DeviceTypeService deviceTypeService;

    @PostMapping("/getEncryptionCells")
    @Operation(summary = "根据log手机号获取机型信息", description = "根据log手机号获取机型信息")
    @ApiResponses(value = {@ApiResponse(responseCode = "500", description = "INTERNAL_SERVER_ERROR")})
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
