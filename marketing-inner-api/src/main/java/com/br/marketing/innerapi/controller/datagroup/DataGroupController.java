package com.br.marketing.innerapi.controller.datagroup;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.datagroup.DataGroupConfgDTO;
import com.br.marketing.vo.datagroup.DataGroupConfigVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.br.marketing.service.datagroup.DataGroupHandlerService;


import java.util.List;

/**
 * 数据分组相关接口
 *
 * @author zhen.Li1
 * @date 2024/11/07
 */
@RestController
@RequestMapping(value = "/data/group")
@Api(value = "数据分组相关接口", tags = "数据分组相关接口")
@Slf4j
public class DataGroupController {


    @Autowired
    private DataGroupHandlerService dataGroupHandlerService;


    @GetMapping("/list")
    @ApiOperation(value = "数据分组配置列表", notes = "数据分组配置列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "上传记录Id,可传多个，分割", paramType = "query", dataType = "string"),
            @ApiImplicitParam(name = "apiCode", value = "apiCode", paramType = "query", dataType = "string")
    })
    public ApiResult<List<DataGroupConfigVO>> list(
            @RequestParam(required = true) String ids,
            @RequestParam(required = true) String apiCode

    ) {
        return new ApiResult<List<DataGroupConfigVO>>().success(
                dataGroupHandlerService.configList(ids, apiCode));
    }


    @ApiOperation(value = "分组配置编辑", notes = "分组配置编辑")
    @PostMapping("/editConfig")
    public Result editConfig(@RequestBody DataGroupConfgDTO dto) {
        return dataGroupHandlerService.updateConfig(dto);

    }

    @ApiOperation(value = "新增或删除配置", notes = "新增或删除配置")
    @PostMapping("/addOrDelete")
    public Result addOrDeleteConfig(@RequestBody DataGroupConfgDTO dto) {
        return dataGroupHandlerService.addOrDeleteConfig(dto);
    }


}
