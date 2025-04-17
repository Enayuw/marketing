package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.service.ICustomerConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链相关调用交互
 */
@RestController
@RequestMapping(value = "/marketing/linkgo")
@Api(value = "短链相关调用")
public class MarketingLinkGoController {


    @Autowired
    private ICustomerConfigService customerConfigService;

    @ApiOperation(value = "获取3k的加密方式")
    @PostMapping("/getThreeKeyEncryptType")
    public ApiResult<Integer> getThreeKeyEncryptType(String apiCode) {
        ApiResult<Integer> apiResult = new ApiResult();
        Result<Integer> result = customerConfigService.getEncryptyType(apiCode);
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            return new ApiResult<Integer>().success(result.getData());
        } else {
            return new ApiResult<Integer>().fail(result.getMessage());
        }

    }


}
