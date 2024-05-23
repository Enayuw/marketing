package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.dataclean.DataCleanRuleDetailDTO;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.innerapi.service.dataclean.DataCleanHandlerService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据清洗任务页面Controller
 * <p>
 * --------------------------------
 *
 * @BelongsProject: marketing
 * @Description: 数据清洗页面
 * @CreateTime: 2024-05-22 15 :28
 * @Version: 1.0
 * @Author: zhen.Li1
 * ------------------------------
 */
@RestController
@RequestMapping(value = "/dataclean/task")
public class DataCleanTaskController {

    /**
     * CODE_1
     */
    private static final Integer CODE_1 = Integer.valueOf(1);
    @Autowired
    private DataCleanHandlerService dataCleanHandlerService;


    @ApiOperation(value = "获取文件信息", notes = "获取文件信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileNames", value = "文件名称集合，多个,号分割", required = true, dataType = "String"),
            @ApiImplicitParam(name = "apiCode", value = "apiCode", required = true, dataType = "String")
    })
    @GetMapping("/getfileMsg")
    public ApiResult<MarketingCleanDataFile> getfileMsg(String fileNames, String apiCode) {
        return new ApiResult().fromResult(dataCleanHandlerService.getfileMsg(fileNames, apiCode), CODE_1);

    }


    @ApiOperation(value = "获取文件名称集合", notes = "获取文件名称集合")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileType", value = "文件类型", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "apiCode", value = "apiCode", required = true, dataType = "String")
    })
    @GetMapping("/getfileNames")
    public ApiResult<List> getfileNames(Integer fileType, String apiCode) {

        List<String> fileNames = dataCleanHandlerService.getfileNames(fileType, apiCode);
        return new ApiResult<List>().setData(fileNames).success();

    }


    @ApiOperation(value = "保存清洗任务")
    @PostMapping("/saveTask")
    public ApiResult<Long> saveTask(@RequestBody DataCleanRuleDetailDTO dto) {
        return new ApiResult<Long>().fromResult(dataCleanHandlerService.saveTask(dto), CODE_1);
    }


    @ApiOperation(value = "获取表头映射", notes = "获取表头映射")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileType", value = "文件类型", required = true, dataType = "Integer"),
    })
    @GetMapping("/getfieldMap")
    public ApiResult<List> getfieldMap(Integer fileType) {

        List<String> fieldMap = dataCleanHandlerService.getfieldMap(fileType);
        return new ApiResult<List>().setData(fieldMap).success();

    }


    @GetMapping("/list")
    @ApiOperation(value = "清洗任务列表", notes = "清洗任务列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "apiCode", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "fileType",value = "文件类型",paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "status",value = "状态",paramType = "query", dataType = "string")
    })
    public ApiResult<PageResultReturn> list(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String apiCode
            , @RequestParam(required = false) String fileType
            , @RequestParam(required = false) String status
    ) {
        return new ApiResult<PageResultReturn>().success(
                dataCleanHandlerService.taskList(current, size, apiCode, fileType, status));
    }


}
