package com.br.marketing.innerapi.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.service.Impl.xc.XieChengCollidingRuleService;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengCollidingStagingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengPackageVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleConfirmParam;
import com.br.marketing.vo.xiecheng.param.CollidingRuleListParam;
import com.br.marketing.vo.xiecheng.param.UpdateCollidingSwitchParam;
import com.br.marketing.vo.xiecheng.param.UpdatePriorityParam;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

/**
 * 携程定制化相关接口
 *
 * @author senyang.zheng
 * @date 2024/04/21
 */
@RestController
@RequestMapping(value = "/xiecheng")
@Api(value = "携程定制化相关接口", tags = "携程定制化相关接口")
@Slf4j
public class XiechengCollidingRuleController {

    @Resource
    private XieChengCollidingRuleService xieChengCollidingRuleService;

    @ApiOperation(value = "1-获取调度任务列表-False-分页")
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

    @ApiOperation(value = "2-获取调度任务列表-True-不分页")
    @GetMapping("/rule/list/true")
    public ApiResult<List<XiechengCollidingRuleVO>> getCollidingRuleTrueList(CollidingRuleListParam listParam) {
        try {
            return new ApiResult<List<XiechengCollidingRuleVO>>().success(xieChengCollidingRuleService.getCollidingRuleTrueList(listParam));
        } catch (Exception e) {
            log.error("获取调度任务列表-True-不分页异常", e);
            return new ApiResult<List<XiechengCollidingRuleVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "3-修改包优先级")
    @PostMapping("/package/priority")
    public ApiResult<Boolean> updatePriority(UpdatePriorityParam param) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.updatePriority(param));
        } catch (Exception e) {
            log.error("修改包优先级异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "4-查看撞库规则详情")
    @GetMapping("/rule/detail")
    public ApiResult<XiechengCollidingRuleVO> getPackageRuleDetail(@RequestParam Long dprId) {
        try {
            return new ApiResult<XiechengCollidingRuleVO>().success(xieChengCollidingRuleService.getCollidingRuleDetail(dprId));
        } catch (Exception e) {
            log.error("查看撞库规则详情异常", e);
            return new ApiResult<XiechengCollidingRuleVO>().fail(ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "5-变更任务状态")
    @PostMapping("/rule/collidingSwitch")
    public ApiResult<Boolean> updateCollidingSwitch(UpdateCollidingSwitchParam param) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.updateCollidingSwitch(param));
        } catch (Exception e) {
            log.error("变更任务状态异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "6-批量删除撞库规则多个id','隔开")
    @PostMapping("/rule/delete")
    public ApiResult<Boolean> deleteCollidingRules(@RequestParam String dprIds) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.deleteCollidingRules(dprIds));
        } catch (Exception e) {
            log.error("批量删除撞库规则异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "7-获取撞库数据包下拉列表-不分页")
    @GetMapping("/package/list")
    public ApiResult<List<XiechengPackageVO>> getPackageList() {
        try {
            return new ApiResult<List<XiechengPackageVO>>().success(xieChengCollidingRuleService.getPackageList());
        } catch (Exception e) {
            log.error("获取撞库数据包下拉列表异常", e);
            return new ApiResult<List<XiechengPackageVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "8-撞库规则暂存列表-不分页")
    @GetMapping("/rule/staging/list")
    public ApiResult<List<XiechengCollidingStagingRuleVO>> getCollidingRuleStagingList() {
        try {
            return new ApiResult<List<XiechengCollidingStagingRuleVO>>().success(xieChengCollidingRuleService.getCollidingRuleStagingList());
        } catch (Exception e) {
            log.error("撞库规则暂存列表异常", e);
            return new ApiResult<List<XiechengCollidingStagingRuleVO>>().fail(ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "9-确认撞库规则")
    @PostMapping("/rule/staging")
    public ApiResult<Boolean> confirmCollidingRule(CollidingRuleConfirmParam confirmParam) {
        try {
            return new ApiResult<Boolean>().success(xieChengCollidingRuleService.confirmCollidingRule(confirmParam));
        } catch (Exception e) {
            log.error("确认撞库规则异常", e);
            return new ApiResult<Boolean>().fail(ServiceResultEnum.FAILED);
        }
    }

    @ApiOperation(value = "10-保存撞库规则")
    @PostMapping("/rule/save")
    public ApiResult<Boolean> saveCollidingRule() {
        return xieChengCollidingRuleService.saveCollidingRule();
    }

}
