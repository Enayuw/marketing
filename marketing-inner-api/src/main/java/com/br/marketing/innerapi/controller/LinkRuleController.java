package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.dto.linkgo.CreateTaskDataDTO;
import com.br.marketing.service.LinkRuleService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/link/rule")
public class LinkRuleController {

    @Resource
    private LinkRuleService linkRuleService;

    @PostMapping("/createTask")
    @ApiOperation(value = "Create export tasks in batch", notes = "Receive task data list from marketingkit_cn project", httpMethod = "POST")
    public ApiResult<Boolean> createTask(@RequestBody List<CreateTaskDataDTO> taskDataList) {
        try {
            log.info("Received create export tasks request: taskCount={}", taskDataList != null ? taskDataList.size() : 0);
            
            Boolean result = linkRuleService.batchCreateTasks(taskDataList);
            return new ApiResult<Boolean>().success(result);
        } catch (Exception ex) {
            log.error("Failed to create export tasks: {}", ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail("Failed to create export tasks!");
        }
    }

}
