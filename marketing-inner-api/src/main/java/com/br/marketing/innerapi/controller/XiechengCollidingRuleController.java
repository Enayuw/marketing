package com.br.marketing.innerapi.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.service.Impl.xc.XieChengPackageRuleService;
import com.br.marketing.vo.xiecheng.PackageRuleListParam;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;

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
    private XieChengPackageRuleService xieChengPackageRuleService;

    @ApiOperation(value = "1-获取调度任务列表")
    @GetMapping("/rule/list")
    public ApiResult<PageResultReturn<XiechengCollidingRuleVO>> getPackageRuleList(PackageRuleListParam listParam) {

        try {
            PageResultReturn<XiechengCollidingRuleVO> list = xieChengPackageRuleService.getPackageRuleList(listParam);
            return new ApiResult<PageResultReturn<XiechengCollidingRuleVO>>().success(list);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new ApiResult<PageResultReturn<XiechengCollidingRuleVO>>().fail(ServiceResultEnum.FAILED);
        }
    }
}
