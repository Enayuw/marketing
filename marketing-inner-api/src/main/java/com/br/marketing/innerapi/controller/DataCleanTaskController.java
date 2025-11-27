package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.dataclean.DataCleanRuleDetailDTO;
import com.br.marketing.entity.MarketingCleanDataFile;
import com.br.marketing.entity.MarketingCleanDataTask;
import com.br.marketing.innerapi.service.dataclean.DataCleanHandlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "数据清洗任务页面", description = "数据清洗任务页面")
public class DataCleanTaskController {

    /**
     * CODE_1
     */
    private static final Integer CODE_1 = Integer.valueOf(1);
    @Autowired
    private DataCleanHandlerService dataCleanHandlerService;


    @Operation(summary = "获取文件信息", description = "获取文件信息")
    @Parameters({
            @Parameter(name = "fileIds", description = "文件ID集合，多个,号分割", required = true),
            @Parameter(name = "apiCode", description = "apiCode", required = true)
    })
    @GetMapping("/getfileMsg")
    public ApiResult<List<MarketingCleanDataFile>> getfileMsg(String fileIds, String apiCode) {
        return new ApiResult().fromResult(dataCleanHandlerService.getfileMsg(fileIds, apiCode), CODE_1);

    }


    @Operation(summary = "获取文件名称集合", description = "获取文件名称集合")
    @Parameters({
            @Parameter(name = "fileType", description = "文件类型", required = true),
            @Parameter(name = "apiCode", description = "apiCode", required = true)
    })
    @GetMapping("/getfileNames")
    public ApiResult<List<MarketingCleanDataFile>> getfileNames(Integer fileType, String apiCode) {

        List<MarketingCleanDataFile> fileNames = dataCleanHandlerService.getfileNames(fileType, apiCode);
        return new ApiResult<List<MarketingCleanDataFile>>().setData(fileNames).success();

    }


    @Operation(summary = "保存/编辑清洗任务")
    @PostMapping("/saveOrUpdateTask")
    public ApiResult<Long> saveOrUpdateTask(@RequestBody DataCleanRuleDetailDTO dto) {
        return new ApiResult<Long>().fromResult(dataCleanHandlerService.saveOrUpdateTask(dto), CODE_1);
    }


    @Operation(summary = "获取表头映射", description = "获取表头映射")
    @Parameters({
            @Parameter(name = "fileType", description = "文件类型", required = true),
    })
    @GetMapping("/getfieldMap")
    public ApiResult<List> getfieldMap(Integer fileType) {

        List<String> fieldMap = dataCleanHandlerService.getfieldMap(fileType);
        return new ApiResult<List>().setData(fieldMap).success();

    }


    @GetMapping("/list")
    @Operation(summary = "清洗任务列表", description = "清洗任务列表")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "apiCode", description = "apiCode")
            , @Parameter(name = "fileType", description = "文件类型")
            , @Parameter(name = "status", description = "状态")
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


    @Operation(summary = "运行清洗任务")
    @PostMapping("/runTask")
    public ApiResult<Long> runTask(@RequestBody DataCleanRuleDetailDTO dto) {
        return new ApiResult<Long>().fromResult(dataCleanHandlerService.runTask(dto), CODE_1);
    }

    @Operation(summary = "试跑清洗任务")
    @PostMapping("/testTask")
    public ApiResult<Long> testTask(@RequestBody DataCleanRuleDetailDTO dto) {
        return new ApiResult<Long>().fromResult(dataCleanHandlerService.testTask(dto), CODE_1);
    }



    @Operation(summary = "获取清洗任务", description = "获取清洗任务")
    @Parameters({
            @Parameter(name = "Id", description = "任务ID")
    })
    @GetMapping("/getTaskByID")
    public ApiResult<MarketingCleanDataTask> getTaskByID(Long Id) {

        return new ApiResult().fromResult(dataCleanHandlerService.getTaskByID(Id), CODE_1);

    }


}
