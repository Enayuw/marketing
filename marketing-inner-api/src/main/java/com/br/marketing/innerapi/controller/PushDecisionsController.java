package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.*;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
import com.br.marketing.service.PushDecisionsService;
import com.br.marketing.vo.PushDecisionsDetailVO;
import com.br.marketing.vo.ReachStrategyVO;
import com.br.marketing.vo.TaskTemplateVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * @ClassName PushDecisionsController
 * @Description 推送决策配置
 * @Author kongbx
 * @Date 2024/8/9 10:17
 */
@RestController
@RequestMapping("/pushDecisions")
@Tag(name = "PushDecisionsController", description = "PushDecisionsController")
public class PushDecisionsController {

    private static final Integer CODE_1 = Integer.valueOf(1);

    @Autowired
    private PushDecisionsService pushDecisionsService;

    @Operation(summary = "推送决策自动化配置", description = "保存接口")
    @PostMapping("/savePushDecisions")
    public ApiResult<Long> savePushDecisions(@RequestBody PushDecisionsDTO dto) {
        return new ApiResult<Long>().fromResult(pushDecisionsService.savePushDecisions(dto), CODE_1);
    }

    @Operation(summary = "删除决策自动化配置")
    @GetMapping("/deletePushDecisions")
    public ApiResult<Boolean> deletePushDecisions(@RequestParam Long id) {
        return new ApiResult<Boolean>().fromResult(pushDecisionsService.deletePushDecisions(id), CODE_1);
    }

    @Operation(summary = "编辑页面")
    @PostMapping("/updatePushDecisions")
    public ApiResult<Long> updatePushDecisions(@RequestBody PushDecisionsDTO dto) {
        return new ApiResult<Long>().fromResult(pushDecisionsService.updatePushDecisions(dto), CODE_1);
    }

    @Operation(summary = "获取推决策模板")
    @GetMapping("/getDecisionsByRule")
    public ApiResult<List<PushDecisionsDetailVO>> getDecisionsByRule(String apiCode) {
        return new ApiResult<List<PushDecisionsDetailVO>>().fromResult(pushDecisionsService.getDecisionsByRule(apiCode), CODE_1);
    }

    @Operation(summary = "获取推送决策列表")
    @PostMapping("/getPushDecisionsList")
    @AddDataAuthBusiness
    public ApiResult<PageResultReturn<PushDecisionsDetailVO>> getPushDecisionsList(@RequestBody SearchConditionDTO dto) {
        return new ApiResult<PageResultReturn<PushDecisionsDetailVO>>().fromResult(pushDecisionsService.getPushDecisionsList(dto), CODE_1);
    }

    @Operation(summary = "获取推送决策详情")
    @GetMapping("/getPushDecisionsDetails")
    public ApiResult<PushDecisionsDetailVO> getPushDecisionsDetails(@RequestParam Long id) {
        return new ApiResult<PushDecisionsDetailVO>().fromResult(pushDecisionsService.getPushDecisionsDetails(id), CODE_1);
    }

    @Operation(summary = "修改规则模板")
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody OptConditionDTO dto) {
        return new ApiResult().fromResult(pushDecisionsService.updateStatus(dto), CODE_1);
    }

    @Operation(summary = "根据依赖模板查询跑分任务")
    @PostMapping("/getRunTaskByTemplate")
    public ApiResult<List<TaskTemplateVO>> getRunTaskByTemplate(@RequestBody RunTaskDTO dto) {
        return new ApiResult<List<TaskTemplateVO>>().fromResult(pushDecisionsService.getRunTaskByTemplate(dto), CODE_1);
    }

    @Operation(summary = "根据apiCode查询触达策略")
    @GetMapping("/getReachStrategyByApiCode")
    public ApiResult<List<ReachStrategyVO>> getReachStrategyByApiCode(@RequestParam String apiCode) {
        return new ApiResult<List<ReachStrategyVO>>().fromResult(pushDecisionsService.getReachStrategyByApiCode(apiCode), CODE_1);
    }

}
