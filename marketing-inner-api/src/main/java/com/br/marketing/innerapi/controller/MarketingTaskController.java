package com.br.marketing.innerapi.controller;

import IceInternal.Ex;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.MarketingTask;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import com.br.marketing.mapper.MarketingTaskMapper;
import com.br.marketing.service.FastTaskRuleService;
import com.br.marketing.service.MarketingTaskService;
import com.br.marketing.vo.FastTaskRuleDetailVO;
import com.br.marketing.vo.MarketingTaskVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
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
@Api(value = "跑数任务规则", tags = "跑数任务规则", produces = "application/json", consumes = "application/json", protocols = "http")
public class MarketingTaskController {


    @Autowired
    MarketingTaskService marketingTaskService;

    @ApiOperation(value = "跑分记录列表", notes = "跑分记录列表")
    @ApiImplicitParams({@ApiImplicitParam(name = "current", value = "页号", paramType = "query", dataType = "integer", defaultValue = "1")
            , @ApiImplicitParam(name = "size", value = "页大小", paramType = "query", dataType = "integer", defaultValue = "10")
            , @ApiImplicitParam(name = "search", value = "搜索：任务编号/任务名称/CID/APIcode", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "status", value = "使用状态 1-开启；0-关闭", paramType = "query", dataType = "integer")
            , @ApiImplicitParam(name = "createTimeStart", value = "创建时间开始", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "createTimeEnd", value = "创建时间结束", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "updateTimeStart", value = "更新时间开始", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "updateTimeEnd", value = "更新时间结束", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "taskStatus", value = "跑分状态", paramType = "query", dataType = "integer")
    })
    @GetMapping("/list")
    public ApiResult<PageResultReturn> list(@RequestParam(defaultValue = "1") int current
            , @RequestParam(defaultValue = "10") int size
            , @RequestParam(required = false) String search
            , @RequestParam(required = false) Integer status
            , @RequestParam(required = false) String createTimeStart
            , @RequestParam(required = false) String createTimeEnd
            , @RequestParam(required = false) String updateTimeStart
            , @RequestParam(required = false) String updateTimeEnd
            , @RequestParam(required = false) Integer taskStatus
    ) {
        PageResultReturn list = marketingTaskService.list(current, size, search, status,
                createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd, taskStatus);
        return new ApiResult<PageResultReturn>().success(list);
    }


    @ApiOperation(value = "修改跑分任务优先级", notes = "修改跑分任务优先级")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "任务id", paramType = "query", dataType = "string")

            , @ApiImplicitParam(name = "priority", value = "跑分日期", paramType = "query", dataType = "Integer")
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


    @ApiOperation(value = "操作跑分记录状态", notes = "操作跑分记录状态，开启/关闭")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String"),
            @ApiImplicitParam(name = "status", value = "状态(1-开启;2-禁用)", required = true, dataType = "Integer")
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
    @ApiOperation(value = "查看跑分任务", notes = "查看跑分任务")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String")
    @GetMapping("/getTask")
    public ApiResult<MarketingTaskVO> getTask(String id) {
        try {
            MarketingTaskVO vo = marketingTaskService.getTask(id);
            return new ApiResult<MarketingTaskVO>().success(vo);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<MarketingTaskVO>().fail(ServiceResultEnum.FAILED);
        }
    }
    @ApiOperation(value = "查看跑分进度", notes = "查看跑分进度")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "hisFileId", value = "hisFileId", required = true, dataType = "String"),
            @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String")
    })
    @GetMapping("/getTaskPercent")
    public ApiResult<Integer> getTaskPercent(String hisFileId,String id ) {
        try {
            Integer num = marketingTaskService.getTaskPercent(hisFileId,id);
            return new ApiResult<Integer>().success(num);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Integer>().fail(ServiceResultEnum.FAILED);
        }
    }
    @ApiOperation(value = "跑分规则下拉列表", notes = "跑分规则下拉列表")
    @ApiImplicitParam(name = "apiCode", value = "apiCode", paramType = "query", dataType = "string")
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
}
