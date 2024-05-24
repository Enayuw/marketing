package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.dataclean.DataCleanConfigDTO;
import com.br.marketing.innerapi.service.dataclean.DataCleanHandlerService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据清洗配置页面Controller
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @Description: 数据清洗配置页面
 * @CreateTime: 2024-05-23 19 :28
 * @Version: 1.0
 * @Author: zhen.Li1
 * ------------------------------
 */
@RestController
@RequestMapping(value = "/dataclean/config")
public class DataCleanConfigController {


    @Autowired
    private DataCleanHandlerService dataCleanHandlerService;


    @GetMapping("/list")
    @ApiOperation(value = "清洗配置列表", notes = "清洗配置列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "apiCode", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "fileType", value = "文件类型", paramType = "query", dataType = "string")
    })
    public ApiResult<PageResultReturn> list(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String apiCode
            , @RequestParam(required = false) String fileType
    ) {
        return new ApiResult<PageResultReturn>().success(
                dataCleanHandlerService.configList(current, size, apiCode, fileType));
    }


    @ApiOperation(value = "清洗配置编辑", notes = "清洗配置编辑")
    @PostMapping("/editConfig")
    public Result editConfig(@RequestBody DataCleanConfigDTO dto) {
        return dataCleanHandlerService.updateConfig(dto);

    }

    @ApiOperation(value = "保存配置", notes = "保存配置")
    @PostMapping("/saveConfig")
    public Result saveConfig(@RequestBody DataCleanConfigDTO dto) {
        return dataCleanHandlerService.saveConfig(dto);
    }

}
