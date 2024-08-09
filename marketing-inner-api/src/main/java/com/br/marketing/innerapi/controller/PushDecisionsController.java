package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.*;
import com.br.marketing.service.PushDecisionsService;
import com.br.marketing.vo.PushDecisionsDetailVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jdk.nashorn.internal.objects.annotations.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @ClassName PushDecisionsController
 * @Description 推送决策配置
 * @Author kongbx
 * @Date 2024/8/9 10:17
 */
@RestController
@RequestMapping("/pushDecisions")
@Api(value = "PushDecisionsController")
public class PushDecisionsController {

    private static final Integer CODE_1 = Integer.valueOf(1);

    @Autowired
    private PushDecisionsService pushDecisionsService;

    @ApiOperation(value = "推送决策自动化配置", notes = "保存接口")
    @PostMapping("/savePushDecisions")
    public ApiResult<Long> saveCondition(@RequestBody PushDecisionsDTO dto) {
        return new ApiResult<Long>().fromResult(pushDecisionsService.savePushDecisions(dto), CODE_1);
    }

    @ApiOperation(value = "删除决策自动化配置")
    @GetMapping("/deletePushDecisions")
    public ApiResult<Boolean> deletePushDecisions(@RequestParam Long id) {
        return new ApiResult<Boolean>().fromResult(pushDecisionsService.deletePushDecisions(id), CODE_1);
    }

    @ApiOperation(value = "获取推送决策列表")
    @PostMapping("/getPushDecisionsList")
    public ApiResult<PageResultReturn<PushDecisionsDetailVO>> getPushDecisionsList(@RequestBody SearchConditionDTO dto) {
        return new ApiResult<PageResultReturn<PushDecisionsDetailVO>>().fromResult(pushDecisionsService.getPushDecisionsList(dto), CODE_1);
    }

    @ApiOperation(value = "获取推送决策详情")
    @GetMapping("/getPushDecisionsDetails")
    public ApiResult<PushDecisionsDetailVO> getPushDecisionsDetails(@RequestParam Long id) {
        return new ApiResult<PushDecisionsDetailVO>().fromResult(pushDecisionsService.getPushDecisionsDetails(id), CODE_1);
    }

    @ApiOperation(value = "修改规则模板")
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody OptConditionDTO dto) {
        return new ApiResult().fromResult(pushDecisionsService.updateStatus(dto), CODE_1);
    }

}
