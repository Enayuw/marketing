package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.service.IProductResultSimpleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 跑分配置
 */
@RestController
@RequestMapping(value = "/rule/product")
@Tag(value = "产品配置", tags = "产品配置", produces = "application/json", consumes = "application/json", protocols = "http")
public class RuleOfProductController {

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    @GetMapping("/getPorductContent")
    @Operation(summary = "获取产品集合信息")
    public ApiResult<String> getPorductContent(){
        return new ApiResult<String>().fromResult(iProductResultSimpleService.getFlagProductStr(),1);
    }


    @PostMapping("/updateProductContent")
    @Operation(summary = "修改产品集合信息")
    public ApiResult updateProductContent(@RequestParam("flagScoreContent") String flagScoreContent){
        return new ApiResult().fromResult(iProductResultSimpleService.updateFlagProduct(flagScoreContent),1);
    }
}
