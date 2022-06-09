package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.innerapi.service.ResourceAllocationService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/backend/resource")
public class BackEndResourceController {

    private static final Logger log = LoggerFactory.getLogger(ResourceAllocationController.class);

    @Autowired
    ResourceAllocationService resourceAllocationService;

    @ApiOperation(value = "新增zk节点信息",notes = "新增zk节点")
    @GetMapping("/createZkData")
    public ApiResult<Boolean> createZkData(String path, String data){
        try {
            Boolean flag = resourceAllocationService.createZkData(path,data);
            return new ApiResult<Boolean>().success(flag);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<Boolean>().success(Boolean.FALSE);
    }

    @ApiOperation(value = "修改zk节点信息",notes = "修改zk节点信息")
    @GetMapping("/setNodeData")
    public ApiResult<Boolean> setNodeData(String path,String data){
        try {
            Boolean flag = resourceAllocationService.setNodeData(path,data);
            return new ApiResult<Boolean>().success(flag);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<Boolean>().success(Boolean.FALSE);
    }

    @ApiOperation(value = "删除zk节点信息",notes = "删除zk节点信息")
    @GetMapping("/deleteZkData")
    public ApiResult<Boolean> deleteZkData(String path){
        try {
            Boolean flag = resourceAllocationService.deleteZkData(path);
            return new ApiResult<Boolean>().success(flag);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<Boolean>().success(Boolean.FALSE);
    }
}
