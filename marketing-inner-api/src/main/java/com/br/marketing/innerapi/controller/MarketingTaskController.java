package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.TaskSelectSaveDTO;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.service.MarketingTaskOptService;
import com.br.marketing.vo.MarketingTaskVO;
import com.br.marketing.vo.ResultPreviewVO;
import com.br.marketing.vo.StatisticsDataDayVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 跑分任务前端控制器
 * @Date 2022/5/10 11:53 AM
 * ------------------------------
 */

@RestController
@Configuration
@RequestMapping("/rule/task")
@Slf4j
@Tag(name = "跑数任务规则", description = "跑数任务规则")
public class MarketingTaskController {


    @Autowired
    MarketingTaskService marketingTaskService;

    @Autowired
    MarketingTaskOptService marketingTaskOptService;

    @Operation(summary = "跑分记录列表", description = "跑分记录列表")
    @Parameters({@Parameter(name = "current", description = "页号")
            , @Parameter(name = "size", description = "页大小")
            , @Parameter(name = "search", description = "搜索：任务编号/任务名称/CID/APIcode")
            , @Parameter(name = "status", description = "使用状态 1-开启；0-关闭")
            , @Parameter(name = "createTimeStart", description = "创建时间开始")
            , @Parameter(name = "createTimeEnd", description = "创建时间结束")
            , @Parameter(name = "updateTimeStart", description = "更新时间开始")
            , @Parameter(name = "updateTimeEnd", description = "更新时间结束")
            , @Parameter(name = "taskStatus", description = "跑分状态")
            , @Parameter(name = "execType", description = "任务执行策略 1-一次性全量；2-一次性验证；3-每个任务的周期;4-每日定时")
    })
    @GetMapping("/list")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn> list(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String search
            , @RequestParam(required = false) Integer status
            , @RequestParam(required = false) String createTimeStart
            , @RequestParam(required = false) String createTimeEnd
            , @RequestParam(required = false) String updateTimeStart
            , @RequestParam(required = false) String updateTimeEnd
            , @RequestParam(required = false) Integer taskStatus
            , @RequestParam(required = false) Integer execType
    ) {
        PageResultReturn list = marketingTaskService.list(current, size, search, status,
                createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd, taskStatus, execType);
        return new ApiResult<PageResultReturn>().success(list);
    }


    @Operation(summary = "修改跑分任务优先级", description = "修改跑分任务优先级")
    @Parameters({@Parameter(name = "id", description = "任务id")
            , @Parameter(name = "priority", description = "跑分日期")
    })
    @GetMapping("/editPriority")
    public ApiResult<Boolean> editPriority(@RequestParam(required = true) String id,
                                           @RequestParam(required = true) Integer priority) {
        try {
            return marketingTaskService.editPriority(id, priority);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }


    @Operation(summary = "操作跑分记录状态", description = "操作跑分记录状态，开启/关闭")
    @Parameters({
            @Parameter(name = "id", description = "id", required = true),
            @Parameter(name = "status", description = "状态(1-开启;2-禁用)", required = true)
    })
    @GetMapping("/updateStatusById")
    public ApiResult<Boolean> updateStatusById(String id, Integer status) {
        //查询
        try {
            boolean flag = marketingTaskService.updateStatusById(id, status);
            if (flag) {
                return new ApiResult<Boolean>().success(true, "操作成功！");
            } else {
                return new ApiResult<Boolean>().success(false, "操作失败！");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "查看跑分任务", description = "查看跑分任务")
    @Parameter(name = "id", description = "id", required = true)
    @GetMapping("/getTask")
    @AddDataAuthBusiness
    public ApiResult<MarketingTaskVO> getTask(String id) {
        try {
            MarketingTaskVO vo = marketingTaskService.getTask(id);
            return new ApiResult<MarketingTaskVO>().success(vo);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<MarketingTaskVO>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "查看跑分进度", description = "查看跑分进度")
    @Parameters({
            @Parameter(name = "hisFileId", description = "hisFileId", required = true),
            @Parameter(name = "id", description = "id", required = true)
    })
    @GetMapping("/getTaskPercent")
    public ApiResult<Long> getTaskPercent(String hisFileId, String id) {
        try {
            Long num = marketingTaskService.getTaskPercent(hisFileId, id);
            return new ApiResult<Long>().success(num);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Long>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "跑分规则下拉列表", description = "跑分规则下拉列表")
    @Parameter(name = "apiCode", description = "apiCode")
    @GetMapping("/getScoreRules")
    public ApiResult<List<ScoreRuleConfig>> getScoreRules(@RequestParam String apiCode) {
        try {
            List<ScoreRuleConfig> list = marketingTaskService.getScoreRules(apiCode);
            return new ApiResult<List<ScoreRuleConfig>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<List<ScoreRuleConfig>>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }

    @Operation(summary = "生成跑分任务")
    @PostMapping("/saveTask")
    public ApiResult<List<Long>> saveTask(@RequestBody TaskSelectSaveDTO dto) {
        return new ApiResult<List<Long>>().fromResult(marketingTaskService.saveTaskSelectV2(dto), 1);
    }

    @Operation(summary = "获取验证数据日期")
    @Parameter(name = "apiCode", description = "apiCode")
    @GetMapping("/getStatisticsDataDay")
    public ApiResult<List<StatisticsDataDayVO>> getStatisticsDataDay(@RequestParam String apiCode) {
        return new ApiResult<List<StatisticsDataDayVO>>().fromResult(marketingTaskService.getStatisticsDataDay(apiCode), 1);
    }

    @Operation(summary = "跑分预览接口", description = "")
    @GetMapping("/resultPreview")
    public ApiResult<ResultPreviewVO> resultPreview(@RequestParam Long taskId) {
        return new ApiResult<ResultPreviewVO>().fromResult(marketingTaskService.resultPreview(taskId), 1);
    }

    @Operation(summary = "删除任务", description = "")
    @GetMapping("/delTask")
    public ApiResult delTask(@RequestParam Long id) {
        return new ApiResult().fromResult(marketingTaskService.delTask(id), 1);
    }

    @Operation(summary = "中止恢复任务", description = "isOrPause 1-暂停；0-恢复")
    @GetMapping("/pauseTask")
    public ApiResult pauseTask(@RequestParam(name = "fileId") Long fileId, @RequestParam(name = "isOrPause") Integer isOrPause) {
        return new ApiResult().fromResult(marketingTaskOptService.pauseTask(fileId, isOrPause), 1);
    }
}
