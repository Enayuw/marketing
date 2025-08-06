package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.service.ScoreDistRuleService;
import com.br.marketing.vo.ScoreDistRuleVo;
import com.br.marketing.vo.bi.AxisWrapVO;
import io.swagger.annotations.ApiOperation;
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

    @ApiOperation(value = "查询评分分布规则模板列表")
    @PostMapping("/getScoreDistRuleList")
    public ApiResult<PageResultReturn<ScoreDistRuleVo>> getScoreDistRuleList(@RequestBody SearchConditionDTO dto) {
        return new ApiResult<PageResultReturn<ScoreDistRuleVo>>().fromResult(scoreDistRuleService.getScoreDistRuleList(dto), CODE_1);
    }

    @ApiOperation(value = "查询评分分布规则模板详情")
    @GetMapping("/getScoreDistRuleDetail")
    public ApiResult<List<AxisWrapVO>> getScoreDistRuleDetail(@RequestParam Long configId) {
        return new ApiResult<List<AxisWrapVO>>().success(scoreDistRuleService.getScoreDistRuleDetail(configId));
    }

    @ApiOperation(value = "评分分布规则模板禁用")
    @PatchMapping("/forbScoreDistRule")
    public ApiResult forbScoreDistRule(@RequestBody Long configId) {
        return new ApiResult().fromResult(scoreDistRuleService.forbScoreDistRule(configId), CODE_1);
    }

    @ApiOperation(value = "评分分布规则模板启用")
    @PatchMapping("/enableScoreDistRule")
    public ApiResult enableScoreDistRule(@RequestBody Long configId) {
        return new ApiResult().fromResult(scoreDistRuleService.enableScoreDistRule(configId), CODE_1);
    }

    @ApiOperation(value = "评分分布规则模板删除")
    @PatchMapping("/deleteScoreDistRule")
    public ApiResult deleteScoreDistRule(@RequestBody Long configId) {
        return new ApiResult().fromResult(scoreDistRuleService.deleteScoreDistRule(configId), CODE_1);
    }






}
