package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.innerapi.service.ResourceAllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 资源配置
 */
@RestController
@RequestMapping("/rule/resource")
@Tag(name = "资源配置", description = "资源配置")
public class ResourceAllocationController {

    private static final Logger log = LoggerFactory.getLogger(ResourceAllocationController.class);

    @Autowired
    ResourceAllocationService resourceAllocationService;

    @Operation(summary = "读取线程池信息", description = "读取线程池信息")
    @GetMapping("/getPushInfoList")
    public ApiResult<List<Map>> getThreadPoolData(){
        try {
            List<Map> list = resourceAllocationService.getThreadPoolData();
            return new ApiResult<List<Map>>().success(list);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<List<Map>>().fail(ServiceResultEnum.UNKNOWN_ERROR);
    }


    @Operation(summary = "修改线程数量", description = "修改线程数量")
    @PostMapping("/editThreadPoolNum")
    public ApiResult<Boolean> editThreadPoolNum(@RequestBody List<Map> list){
        try {
            Boolean flag = resourceAllocationService.editThreadPoolNum(list);
            return new ApiResult<Boolean>().success(flag);
        }catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage(),e);
        }
        return new ApiResult<Boolean>().fail(ServiceResultEnum.UNKNOWN_ERROR);
    }

    @Operation(summary = "新增zk节点信息", description = "新增zk节点")
    @GetMapping("/createZkData")
    public ApiResult<Boolean> createZkData(String path,String data){
        try {
            Boolean flag = resourceAllocationService.createZkData(path,data);
            return new ApiResult<Boolean>().success(flag);
        } catch (Exception ex) {
            ex.printStackTrace();
            log.error(ex.getMessage(),ex);
        }
        return new ApiResult<Boolean>().success(Boolean.FALSE);
    }

    @Operation(summary = "修改zk节点信息", description = "修改zk节点信息")
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

    @Operation(summary = "删除zk节点信息", description = "删除zk节点信息")
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
