package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.service.ScoreDistRuleService;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.ScoreDistRuleVo;
import com.br.marketing.vo.bi.AxisWrapVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;

/**
 * 评分分布规则模板controller
 * 技术方案：https://c.100credit.cn/pages/viewpage.action?pageId=217128561
 */
@Slf4j
@RestController
@RequestMapping("/scoreDistRule")
public class ScoreDistRuleController {

    private static final Integer CODE_1 = Integer.valueOf(1);

    @Resource
    ScoreDistRuleService scoreDistRuleService;

    @Operation(summary = "查询评分分布规则模板列表")
    @PostMapping("/getScoreDistRuleList")
    public ApiResult<PageResultReturn<ScoreDistRuleVo>> getScoreDistRuleList(@RequestBody SearchConditionDTO dto) {
        return new ApiResult<PageResultReturn<ScoreDistRuleVo>>().fromResult(scoreDistRuleService.getScoreDistRuleList(dto), CODE_1);
    }

    @Operation(summary = "获取模板")
    @GetMapping("/getScoreDistRuleByApiCode")
    public ApiResult<List<ScoreDistRuleVo>> getScoreDistRuleByApiCode(String apiCode) {
        return new ApiResult<List<ScoreDistRuleVo>>().fromResult(scoreDistRuleService.getScoreDistRuleByApiCode(apiCode), CODE_1);
    }

    @Operation(summary = "查询评分分布规则模板详情")
    @GetMapping("/getScoreDistRuleDetail")
    public ApiResult<List<AxisWrapVO>> getScoreDistRuleDetail(@RequestParam Long configId) {
        return new ApiResult<List<AxisWrapVO>>().success(scoreDistRuleService.getScoreDistRuleDetail(configId));
    }

    @Operation(summary = "评分分布规则模板禁用")
    @PatchMapping("/forbScoreDistRule")
    public ApiResult forbScoreDistRule(@RequestParam Long configId) {
        return new ApiResult().fromResult(scoreDistRuleService.forbScoreDistRule(configId), CODE_1);
    }

    @Operation(summary = "评分分布规则模板启用")
    @PatchMapping("/enableScoreDistRule")
    public ApiResult enableScoreDistRule(@RequestParam Long configId) {
        return new ApiResult().fromResult(scoreDistRuleService.enableScoreDistRule(configId), CODE_1);
    }

    @Operation(summary = "评分分布规则模板删除")
    @PatchMapping("/deleteScoreDistRule")
    public ApiResult deleteScoreDistRule(@RequestParam Long configId) {
        return new ApiResult().fromResult(scoreDistRuleService.deleteScoreDistRule(configId), CODE_1);
    }






}
