package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.service.ResourceAllocationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源配置
 */
@RestController
@RequestMapping("/rule/resource")
@Api(value = "资源配置", tags = "资源配置", produces = "application/json", consumes = "application/json", protocols = "http")
public class ResourceAllocationController {

    private static final Logger log = LoggerFactory.getLogger(ResourceAllocationController.class);

    @Autowired
    ResourceAllocationService resourceAllocationService;

    @ApiOperation(value = "读取线程池信息",notes = "读取线程池信息")
    @GetMapping("/getPushInfoList")
    public ApiResult<Map> getThreadPoolData(){
        try {
            HashMap map = resourceAllocationService.getThreadPoolData();
            return new ApiResult<Map>().success(map);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<Map>().fail(ServiceResultEnum.UNKNOWN_ERROR);
    }


    @ApiOperation(value = "修改线程数量",notes = "修改线程数量")
    @PostMapping("/editThreadPoolNum")
    public ApiResult<Boolean> editThreadPoolNum(@RequestBody Map map){
        try {
            Boolean flag = resourceAllocationService.editThreadPoolNum(map);
            return new ApiResult<Boolean>().success(flag);
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(),e);
        }
        return new ApiResult<Boolean>().fail(ServiceResultEnum.UNKNOWN_ERROR);
    }


}
