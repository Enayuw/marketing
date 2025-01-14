package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.CarClueReportService;
import com.br.marketing.vo.CarClueInfoVo;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;


/**
 * 车线索列表
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
@RestController
@RequestMapping(value = "/car")
@Api(value = "车线索列表", tags = "车线索列表", produces = "application/json", consumes = "application/json", protocols = "http")
public class CarClueReportController {

    @Resource
    private CarClueReportService carClueReportService;


    @PostMapping("/getCarClueList")
    @ApiOperation(value = "车线索数据统计报表列表", notes = "车线索数据统计报表列表")
    @ApiResponses(value = {@ApiResponse(code = 500, message = "INTERNAL_SERVER_ERROR", response = CarClueInfoVo.class)})
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> getReportList(@RequestBody @Valid CarClueReportDTO request) {
        // 调用服务层
        PageResultReturn result = carClueReportService.getReportList(request);
        if (result != null) {
            return new ApiResult<PageResultReturn>().success(result);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

}
