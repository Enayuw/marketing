package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.dto.DataExportTaskDTO;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.service.IDataExportTaskService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;


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

    @Resource
    private IDataExportTaskService dataExportTaskService;

    @PostMapping("/createTask")
    public Result<Long> createTask(@RequestBody @Valid DataExportTaskDTO taskDTO) {
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            Boolean task = dataExportTaskService.createTask(taskDTO, user);
            if (task) {
                return new Result<>().success().setMessage("新增导出任务成功！");
            }
            return new Result<>().failure().setMessage("新增导出任务失败！");
        } catch (Exception e) {
            return new Result<>().failure().setMessage("新增导出任务失败！");
        }
    }
}
