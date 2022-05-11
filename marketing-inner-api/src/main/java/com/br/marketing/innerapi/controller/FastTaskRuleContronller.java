package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.Marketing;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import com.br.marketing.service.FastTaskRuleService;
import com.br.marketing.vo.FastTaskRuleDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 去重规则控制层
 * songjuanjuan
 */
@RestController
@Configuration
@RequestMapping("/rule/fastTask")
@Api(value = "手动跑数任务规则", tags = "手动跑数任务规则", produces = "application/json", consumes = "application/json", protocols = "http")
public class FastTaskRuleContronller {

    private static final Logger log = LoggerFactory.getLogger(FastTaskRuleContronller.class);

    @Autowired
    FastTaskRuleService fastTaskRuleService;

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
        PageResultReturn list = fastTaskRuleService.list(current, size, search, status,
                createTimeStart, createTimeEnd, updateTimeStart, updateTimeEnd, taskStatus);
        return new ApiResult<PageResultReturn>().success(list);
    }



    @ApiOperation(value = "生成批量跑分", notes = "生成批量跑分")
    @PostMapping("/save")
    @Deprecated
    public ApiResult<Boolean> save(@RequestBody @Validated FastTaskRuleDetailVO vo) {
        //获取用户上下文
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            return fastTaskRuleService.save(vo,user);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }


    @ApiOperation(value = "修改批量跑分", notes = "修改批量跑分")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "任务id", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "ruleName", value = "任务名称", paramType = "query", dataType = "string")
            , @ApiImplicitParam(name = "taskTime", value = "跑分日期", paramType = "query", dataType = "string")
    })
    @GetMapping("/update")
    public ApiResult<Boolean> update(@RequestParam(required = false) String id,
                                     @RequestParam(required = false) String ruleName,
                                     @RequestParam(required = false) String taskTime) {
        //获取用户上下文
        try {
            MarketingUserDetail user = ThreadContextInfo.getUser();
            return fastTaskRuleService.update(id,ruleName,taskTime,user);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new ApiResult<Boolean>().fail(false,ServiceResultEnum.FAILED);
        }
    }


    @ApiOperation(value = "查看跑分任务", notes = "查看跑分任务")
    @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "String")
    @GetMapping("/getFastTask")
    public ApiResult<FastTaskRuleDetailVO> getFastTask(String id) {
        try {
            FastTaskRuleDetailVO vo = fastTaskRuleService.getFastTask(id);
            return new ApiResult<FastTaskRuleDetailVO>().success(vo);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<FastTaskRuleDetailVO>().fail(ServiceResultEnum.FAILED);
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
            MarketingUserDetail user = ThreadContextInfo.getUser();
            boolean flag = fastTaskRuleService.updateStatusById(id,status,user);
            if(flag){
                return new ApiResult<Boolean>().success(true,"操作成功！");
            }else {
                return new ApiResult<Boolean>().success(false,"操作失败！");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }


    @ApiOperation(value = "跑分规则下拉列表", notes = "跑分规则下拉列表")
    @ApiImplicitParam(name = "apiCode", value = "apiCode", paramType = "query", dataType = "string")
    @GetMapping("/getScoreRules")
    public ApiResult<List<ScoreRuleConfig>> getScoreRules(@RequestParam String apiCode) {
        try {
            List<ScoreRuleConfig> list = fastTaskRuleService.getScoreRules(apiCode);
            return new ApiResult<List<ScoreRuleConfig>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<List<ScoreRuleConfig>>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }

    @ApiOperation(value = "获取未跑分数据量", notes = "获取未跑分数据量")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "ids", value = "跑分数据所选的数据id，逗号分隔", required = true, dataType = "String"),
            @ApiImplicitParam(name = "apiCode", value = "apiCode", required = true, dataType = "String")
    })
    @GetMapping("/getNum")
    public ApiResult<Integer> getNum(String ids, String apiCode) {
        try {
            Integer num = fastTaskRuleService.getNum(ids, apiCode);
            return new ApiResult<Integer>().success(num);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<Integer>().fail(ServiceResultEnum.FAILED);
        }
    }

}
