package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.bi.ZhongAnControlGroupService;
import com.br.marketing.vo.zhongan.ZhongAnCustomInfoVO;
import com.br.marketing.vo.zhongan.param.ZhongAnControlGroupParam;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(value = "众安对照组配置", tags = "众安对照组配置相关接口")
@Slf4j
public class ZhongAnControlGroupController {

    private static final Integer CODE_1 = Integer.valueOf(1);
    @Resource
    private ZhongAnControlGroupService zhongAnControlGroupService;

    @ApiOperation(value = "获取众安对照组列表")
    @GetMapping("/getCustomInfoList")
    public ApiResult<List<ZhongAnCustomInfoVO>> getCustomInfoList(@RequestParam String reportDate) {
        log.warn("获取众安对照组列表,请求参数{}", reportDate);
        return new ApiResult<List<ZhongAnCustomInfoVO>>().fromResult(zhongAnControlGroupService.getCustomInfoList(reportDate), CODE_1);
    }

    @ApiOperation(value = "保存众安对照组配置")
    @PostMapping("/saveCustomInfo")
    public ApiResult saveCustomInfo(@RequestBody ZhongAnControlGroupParam param) {
        log.warn("众安对照组,请求参数{}", param);
        return new ApiResult().fromResult(zhongAnControlGroupService.saveCustomInfo(param), CODE_1);
    }


}
