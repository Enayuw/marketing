package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.common.exception.validators.ParamValidErrorException;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.service.FastTaskRuleService;
import com.br.marketing.vo.FastTaskRuleDetailVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "手动跑数任务规则", description = "手动跑数任务规则")
public class FastTaskRuleContronller {

    private static final Logger log = LoggerFactory.getLogger(FastTaskRuleContronller.class);

    @Autowired
    FastTaskRuleService fastTaskRuleService;

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
    })
    @GetMapping("/list")
    @Deprecated
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



    @Operation(summary = "生成批量跑分", description = "生成批量跑分")
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


    @Operation(summary = "修改批量跑分", description = "修改批量跑分")
    @Parameters({@Parameter(name = "id", description = "任务id")
            , @Parameter(name = "ruleName", description = "任务名称")
            , @Parameter(name = "taskTime", description = "跑分日期")
    })
    @GetMapping("/update")
    @Deprecated
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


    @Operation(summary = "查看跑分任务", description = "查看跑分任务")
    @Parameter(name = "id", description = "id", required = true)
    @GetMapping("/getFastTask")
    @Deprecated
    public ApiResult<FastTaskRuleDetailVO> getFastTask(String id) {
        try {
            FastTaskRuleDetailVO vo = fastTaskRuleService.getFastTask(id);
            return new ApiResult<FastTaskRuleDetailVO>().success(vo);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<FastTaskRuleDetailVO>().fail(ServiceResultEnum.FAILED);
        }
    }


    @Operation(summary = "操作跑分记录状态", description = "操作跑分记录状态，开启/关闭")
    @Parameters({
            @Parameter(name = "id", description = "id", required = true),
            @Parameter(name = "status", description = "状态(1-开启;2-禁用)", required = true)
    })
    @GetMapping("/updateStatusById")
    @Deprecated
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


    @Operation(summary = "跑分规则下拉列表", description = "跑分规则下拉列表")
    @Parameter(name = "apiCode", description = "apiCode")
    @GetMapping("/getScoreRules")
    @Deprecated
    public ApiResult<List<ScoreRuleConfig>> getScoreRules(@RequestParam String apiCode) {
        try {
            List<ScoreRuleConfig> list = fastTaskRuleService.getScoreRules(apiCode);
            return new ApiResult<List<ScoreRuleConfig>>().success(list);
        } catch (ParamValidErrorException ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<List<ScoreRuleConfig>>().fail(ServiceResultEnum.SUCCESS_1);
        }
    }

    @Operation(summary = "获取未跑分数据量", description = "获取未跑分数据量")
    @Parameters({
            @Parameter(name = "ids", description = "跑分数据所选的数据id，逗号分隔", required = true),
            @Parameter(name = "apiCode", description = "apiCode", required = true)
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
