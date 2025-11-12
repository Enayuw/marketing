package com.br.marketing.innerapi.controller;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.VariableAllocationDTO;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.VariableAllocationService;
import com.br.marketing.vo.VariableAllocationVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 定制化配置
 *
 * @author guangxiu.li@brgroup.com
 * @dateTime 2024/03/21 17:40
 */
@RestController
@RequestMapping(value = "/rule/vac")
@Tag(name = "定制化配置", description = "定制化配置")
public class VariableAllocationController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Resource
    VariableAllocationService variableAllocationService;

    @PostMapping("/getVariableList")
    @Operation(summary = "配置列表", description = "配置列表")
    @AddDataAuthBusiness
    public ApiResult<VariableAllocationVO> getVariableList(@RequestBody VariableAllocationDTO dto) {
        return new ApiResult<VariableAllocationVO>().success(variableAllocationService.getVariableList(dto));
    }

    @PostMapping("/getAllocationValue")
    @Operation(summary = "配置列表", description = "配置列表")
    @AddDataAuthBusiness
    public ApiResult<JSONObject> getAllocationValue(@RequestBody VariableAllocationDTO dto) {
        return new ApiResult<JSONObject>().success(variableAllocationService.getAllocationValue(dto.getApiCode(), dto.getAllocationType()));
    }

    @Operation(summary = "变更配置列表", description = "变更配置列表")
    @PostMapping("/updateVariableList")
    @Parameters({@Parameter(name = "id", description = "id", required = true)})
    public ApiResult<Boolean> updateVariableList(@RequestBody String params) {
        try {
            return variableAllocationService.updateVariableList(params);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "获取配置", description = "获取配置")
    @PostMapping("/getVariableAllocation")
    public VariableAllocationVO getVariableAllocation() {
        return variableAllocationService.getVariableAllocation();
    }
}
