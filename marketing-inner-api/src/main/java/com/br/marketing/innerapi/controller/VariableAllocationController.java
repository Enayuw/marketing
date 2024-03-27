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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;

/**
 * 定制化配置
 *
 * @author guangxiu.li@brgroup.com
 * @dateTime 2024/03/21 17:40
 */
@RestController
@RequestMapping(value = "/rule/vac")
@Api(value = "定制化配置", tags = "定制化配置", produces = "application/json", consumes = "application/json", protocols = "http")
public class VariableAllocationController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    @Resource
    VariableAllocationService variableAllocationService;

    @PostMapping("/getVariableList")
    @ApiOperation(value = "配置列表", notes = "配置列表", httpMethod = "GET")
    @AddDataAuthBusiness
    public ApiResult<VariableAllocationVO> getVariableList(@RequestBody VariableAllocationDTO dto) {
        return new ApiResult<VariableAllocationVO>().success(variableAllocationService.getVariableList(dto));
    }

    @ApiOperation(value = "变更配置列表", notes = "变更配置列表")
    @PostMapping("/updateVariableList")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id", required = true, dataType = "Long"),
        @ApiImplicitParam(name = "normalQuantity", value = "撞得总量级", required = true, paramType = "query", dataType = "int"),
        @ApiImplicitParam(name = "abnormalQuantity", value = "异常总量级", required = true, paramType = "query", dataType = "int")})
    public ApiResult<Boolean> updateVariableList(@RequestBody String params) {
        JSONObject param = JSONObject.parseObject(params);
        Integer normalQuantity = param.getInteger("normalQuantity");
        Integer abnormalQuantity = param.getInteger("abnormalQuantity");
        Long id = param.getLong("id");
        try {
            return variableAllocationService.updateVariableList(id, normalQuantity, abnormalQuantity);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "获取配置", notes = "获取配置")
    @PostMapping("/getVariableAllocation")
    public VariableAllocationVO getVariableAllocation() {
        return variableAllocationService.getVariableAllocation();
    }
}
