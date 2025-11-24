package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.bi.ZhongAnControlGroupService;
import com.br.marketing.vo.zhongan.ZhongAnCustomInfoVO;
import com.br.marketing.vo.zhongan.param.ControlGroupDTO;
import com.br.marketing.vo.zhongan.param.ZhongAnControlGroupParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @ClassName ZhongAnControlGroupController
 * @Description 众安对照组配置
 * @Author kongbx
 * @Date 2024/9/18 11:45
 */
@RestController
@RequestMapping("/controlGroup")
@Tag(name = "众安对照组配置", description = "众安对照组配置相关接口")
@Slf4j
public class ZhongAnControlGroupController {

    private static final Integer CODE_1 = Integer.valueOf(1);
    @Resource
    private ZhongAnControlGroupService zhongAnControlGroupService;

    @Operation(summary = "获取众安对照组列表")
    @PostMapping("/getCustomInfoList")
    public ApiResult<List<ZhongAnCustomInfoVO>> getCustomInfoList(@RequestBody ControlGroupDTO controlGroupDTO) {
        return new ApiResult<List<ZhongAnCustomInfoVO>>().fromResult(zhongAnControlGroupService.getCustomInfoList(controlGroupDTO), CODE_1);
    }

    @Operation(summary = "保存众安对照组配置")
    @PostMapping("/saveCustomInfo")
    public ApiResult saveCustomInfo(@RequestBody ZhongAnControlGroupParam param) {
        log.warn("众安对照组,请求参数{}", param);
        return new ApiResult().fromResult(zhongAnControlGroupService.saveCustomInfo(param), CODE_1);
    }

    @Operation(summary = "查询近一个月配置状态")
    @GetMapping("/getConfigStatus")
    public ApiResult<List<String>> getConfigStatus(@RequestParam String startDate, @RequestParam String endDate) {
        return new ApiResult<List<String>>().fromResult(zhongAnControlGroupService.getConfigStatus(startDate,endDate), CODE_1);
    }

}
