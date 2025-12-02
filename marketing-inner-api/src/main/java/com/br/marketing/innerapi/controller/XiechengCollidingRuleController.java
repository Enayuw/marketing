package com.br.marketing.innerapi.controller;

import java.util.List;
import javax.annotation.Resource;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.dto.rulecenter.XcDeleteTaskQueryDTO;
import com.br.marketing.dto.rulecenter.XcDeleteTaskVO;
import com.br.marketing.vo.xiecheng.param.UpdateRoundParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.br.marketing.aspect.LogRecordAnnotation;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.enums.InterfaceOperationsEnum;
import com.br.marketing.service.Impl.xc.XieChengCollidingRuleService;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengCollidingStagingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengPackageVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleConfirmParam;
import com.br.marketing.vo.xiecheng.param.CollidingRuleListParam;
import com.br.marketing.vo.xiecheng.param.UpdateCollidingRuleParam;
import com.br.marketing.vo.xiecheng.param.UpdateCollidingSwitchParam;
import com.br.marketing.vo.xiecheng.param.UpdatePriorityParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

/**
 * 携程定制化相关接口
 *
 * @author senyang.zheng
 * @date 2024/04/21
 */
@RestController
@RequestMapping(value = "/xiecheng")
@Tag(name = "携程定制化相关接口", description = "携程定制化相关接口")
@Slf4j
public class XiechengCollidingRuleController {

    @Resource
    private XieChengCollidingRuleService xieChengCollidingRuleService;

    @Operation(summary = "1-获取调度任务列表-False-分页")
    @GetMapping("/rule/list/false")
    public ApiResult<PageResultReturn<XiechengCollidingRuleVO>> getCollidingRuleFalseList(CollidingRuleListParam listParam) {
        try {
            PageResultReturn<XiechengCollidingRuleVO> list = xieChengCollidingRuleService.getCollidingRuleFalseList(listParam);
            return new ApiResult<PageResultReturn<XiechengCollidingRuleVO>>().success(list);
        } catch (Exception e) {
            log.error("获取调度任务列表-False-分页异常", e);
            return new ApiResult<PageResultReturn<XiechengCollidingRuleVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "2-获取调度任务列表-True-不分页")
    @GetMapping("/rule/list/true")
    public ApiResult<List<XiechengCollidingRuleVO>> getCollidingRuleTrueList(CollidingRuleListParam listParam) {
        try {
            return new ApiResult<List<XiechengCollidingRuleVO>>().success(xieChengCollidingRuleService.getCollidingRuleTrueList(listParam));
        } catch (Exception e) {
            log.error("获取调度任务列表-True-不分页异常", e);
            return new ApiResult<List<XiechengCollidingRuleVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "3-修改包优先级")
    @PostMapping("/package/priority")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_UPDATE_PRIORITY,
        extendInfo = "修改了{#param.packageName}优先级，由{#param.originalPriority}修改为{#param.priority}")
    public ApiResult<Boolean> updatePriority(UpdatePriorityParam param) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.updatePriority(param));
        } catch (Exception e) {
            log.error("修改包优先级异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "4-查看撞库规则详情")
    @GetMapping("/rule/detail")
    public ApiResult<XiechengCollidingRuleVO> getPackageRuleDetail(@RequestParam Long dprId) {
        try {
            return new ApiResult<XiechengCollidingRuleVO>().success(xieChengCollidingRuleService.getCollidingRuleDetail(dprId));
        } catch (Exception e) {
            log.error("查看撞库规则详情异常", e);
            return new ApiResult<XiechengCollidingRuleVO>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "5-修改撞库规则")
    @PostMapping("/rule/update")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_UPDATE_COLLIDING_RULE,
        extendInfo = "修改了{#param.packageName}中的原撞库时间 {#param.originalCollidingStartTime} —— {#param.originalCollidingEndTime} 的"
            + "设定撞得量级 {#param.originalCollidingBackNumber} 和设定撞库次数 {#param.originalCollidingTimes} "
            + "修改为 {#param.collidingStartTime} —— {#param.collidingEndTime} 的"
            + "设定撞得量级 {#param.collidingBackNumber} 和设定撞库次数 {#param.collidingTimes}")
    public ApiResult<Boolean> updateCollidingRule(UpdateCollidingRuleParam param) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.updateCollidingRule(param));
        } catch (KnowException ke){
            return new ApiResult<Boolean>().fail(false, ke.getMessage());
        } catch (Exception e) {
            log.error("修改撞库规则异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "6-变更任务状态")
    @PostMapping("/rule/collidingSwitch")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_UPDATE_COLLIDING_SWITCH,
        extendInfo = "将{#param.packageName}{#param.collidingSwitch == 0 ? '启用' : '禁用'}")
    public ApiResult<Boolean> updateCollidingSwitch(UpdateCollidingSwitchParam param) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.updateCollidingSwitch(param));
        } catch (Exception e) {
            log.error("变更任务状态异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "7-批量删除撞库规则多个id','隔开")
    @PostMapping("/rule/delete")
    public ApiResult<Boolean> deleteCollidingRules(@RequestParam String dprIds) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.deleteCollidingRules(dprIds));
        } catch (Exception e) {
            log.error("批量删除撞库规则异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "8-获取撞库数据包下拉列表-不分页")
    @GetMapping("/package/list")
    public ApiResult<List<XiechengPackageVO>> getPackageList() {
        try {
            return new ApiResult<List<XiechengPackageVO>>().success(xieChengCollidingRuleService.getPackageList());
        } catch (Exception e) {
            log.error("获取撞库数据包下拉列表异常", e);
            return new ApiResult<List<XiechengPackageVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "9-撞库规则暂存列表-不分页")
    @GetMapping("/rule/staging/list")
    public ApiResult<List<XiechengCollidingStagingRuleVO>> getCollidingRuleStagingList() {
        try {
            return new ApiResult<List<XiechengCollidingStagingRuleVO>>().success(xieChengCollidingRuleService.getCollidingRuleStagingList());
        } catch (Exception e) {
            log.error("撞库规则暂存列表异常", e);
            return new ApiResult<List<XiechengCollidingStagingRuleVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "10-确认撞库规则")
    @PostMapping("/rule/staging")
    public ApiResult<Long> confirmCollidingRule(CollidingRuleConfirmParam confirmParam) {
        try {
            return new ApiResult<Long>().success(xieChengCollidingRuleService.confirmCollidingRule(confirmParam));
        } catch (KnowException ke){
            return new ApiResult<Long>().fail(ke.getMessage());
        } catch (Exception e) {
            log.error("确认撞库规则异常", e);
            return new ApiResult<Long>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "11-删除已确认撞库规则")
    @PostMapping("/rule/staging/delete")
    public ApiResult<Boolean> deleteStagingCollidingRule(@RequestParam Long prsId) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.deleteStagingCollidingRule(prsId));
        } catch (Exception e) {
            log.error("删除已确认撞库规则异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "12-保存撞库规则")
    @PostMapping("/rule/save")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_SAVE_COLLIDING_RULE, extendInfo = "[saveCollidingRuleLog]")
    public ApiResult<Boolean> saveCollidingRule() {
        return xieChengCollidingRuleService.saveCollidingRule();
    }

    @Operation(summary = "13-修改包轮次")
    @PostMapping("/package/round")
    @LogRecordAnnotation(bizNo = InterfaceOperationsEnum.XIECHENG_UPDATE_ROUND,
            extendInfo = "修改了{#param.packageName}轮次，修改为{#param.round}")
    public ApiResult<Boolean> updateRound(UpdateRoundParam param) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.updateRound(param));
        } catch (Exception e) {
            log.error("修改包轮次异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "14-撞库数据剔除任务列表")
    @GetMapping("/collidingDataDeleteTask/list")
    public ApiResult<PageResultReturn<XcDeleteTaskVO>> getCollidingDataDeleteTaskList(XcDeleteTaskQueryDTO queryDTO) {
        try {
            PageResultReturn<XcDeleteTaskVO> list = xieChengCollidingRuleService.getCollidingDataDeleteTaskList(queryDTO);
            return new ApiResult<PageResultReturn<XcDeleteTaskVO>>().success(list);
        } catch (Exception e) {
            log.error("获取撞库数据剔除任务列表-分页异常", e);
            return new ApiResult<PageResultReturn<XcDeleteTaskVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @Operation(summary = "15-撞库数据剔除任务删除")
    @PostMapping("/collidingDataDeleteTask/delete")
    public ApiResult deleteCollidingDataDeleteTask(@RequestParam Long taskId) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.deleteCollidingDataDeleteTask(taskId));
        } catch (Exception e) {
            log.error("撞库数据剔除任务删除异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

}
