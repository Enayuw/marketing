package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.innerapi.config.ThreadContextInfo;
import com.br.marketing.service.IProductResultSimpleService;
import com.br.marketing.service.ScoreRuleConfigService;
import com.br.marketing.vo.ScoreRuleConfigPageVO;
import com.br.marketing.vo.ScoreRuleVO;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 跑分配置
 */
@RestController
@RequestMapping(value = "/rule/product")
@Api(value = "产品配置", tags = "产品配置", produces = "application/json", consumes = "application/json", protocols = "http")
public class RuleOfProductController {

    @Autowired
    IProductResultSimpleService iProductResultSimpleService;

    @GetMapping("/getPorductContent")
    @ApiOperation(value = "获取产品集合信息")
    public ApiResult<String> getPorductContent(){
        return new ApiResult<String>().fromResult(iProductResultSimpleService.getFlagProductStr(),1);
    }


    @GetMapping("/updateProductContent")
    @ApiOperation(value = "修改产品集合信息")
    public ApiResult updateProductContent(@RequestParam("flagScoreContent") String flagScoreContent){
        return new ApiResult().fromResult(iProductResultSimpleService.updateFlagProduct(flagScoreContent),1);
    }
}
