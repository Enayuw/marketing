package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.dataclean.DataCleanRuleDetailDTO;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.innerapi.service.dataclean.DataCleanHandlerService;
import io.swagger.annotations.Api;
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
@Api(value = "数据清洗任务页面", tags = "数据清洗任务页面", produces = "application/json", consumes = "application/json", protocols = "http")
public class DataCleanTaskController {

    /**
     * CODE_1
     */
    private static final Integer CODE_1 = Integer.valueOf(1);
    @Autowired
    private DataCleanHandlerService dataCleanHandlerService;


    @ApiOperation(value = "获取文件信息", notes = "获取文件信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileIds", value = "文件ID集合，多个,号分割", required = true, dataType = "String"),
            @ApiImplicitParam(name = "apiCode", value = "apiCode", required = true, dataType = "String")
    })
    @GetMapping("/getfileMsg")
    public ApiResult<List<MarketingCleanDataFile>> getfileMsg(String fileIds, String apiCode) {
        return new ApiResult().fromResult(dataCleanHandlerService.getfileMsg(fileIds, apiCode), CODE_1);

    }


    @ApiOperation(value = "获取文件名称集合", notes = "获取文件名称集合")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fileType", value = "文件类型", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "apiCode", value = "apiCode", required = true, dataType = "String")
    })
    @GetMapping("/getfileNames")
    public ApiResult<List<MarketingCleanDataFile>> getfileNames(Integer fileType, String apiCode) {

        List<MarketingCleanDataFile> fileNames = dataCleanHandlerService.getfileNames(fileType, apiCode);
        return new ApiResult<List<MarketingCleanDataFile>>().setData(fileNames).success();

    }


    @ApiOperation(value = "保存/编辑清洗任务")
    @PostMapping("/saveOrUpdateTask")
    public ApiResult<Long> saveOrUpdateTask(@RequestBody DataCleanRuleDetailDTO dto) {
        return new ApiResult<Long>().fromResult(dataCleanHandlerService.saveOrUpdateTask(dto), CODE_1);
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
            , @ApiImplicitParam(name = "fileType", value = "文件类型", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "status", value = "状态", paramType = "query", dataType = "string")
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


    @ApiOperation(value = "运行清洗任务")
    @PostMapping("/runTask")
    public ApiResult<Long> runTask(@RequestBody DataCleanRuleDetailDTO dto) {
        return new ApiResult<Long>().fromResult(dataCleanHandlerService.runTask(dto), CODE_1);
    }

    @ApiOperation(value = "试跑清洗任务")
    @PostMapping("/testTask")
    public ApiResult<Long> testTask(@RequestBody DataCleanRuleDetailDTO dto) {
        return new ApiResult<Long>().fromResult(dataCleanHandlerService.testTask(dto), CODE_1);
    }


}
