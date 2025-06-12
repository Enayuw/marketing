package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.DataExportTaskDTO;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;


/**
 * 中台数据导出
 * @author guangxiu.li
 * @date 2025/06/10
 * @description
 */
@RestController
@RequestMapping(value = "/export")
@Api(value = "中台数据导出", tags = "中台数据导出", produces = "application/json", consumes = "application/json", protocols = "http")
public class DataExportController {

//    @Autowired
//    private IDataExportTaskService dataExportTaskService;

    @PostMapping("/createTask")
    public Result<Long> createTask(@RequestBody DataExportTaskDTO taskDTO) {
//        try {
//            Long taskId = dataExportTaskService.createTask(taskDTO);
//            return new Result<>().success().setDate(taskId);
//        } catch (Exception e) {
            return new Result<>().failure().setMessage("新增导出任务失败！");

//        }
    }
}
