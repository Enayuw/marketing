package com.br.marketing.innerapi.controller;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.linkgo.CustomerEncryptDTO;
import com.br.marketing.service.ICustomerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链相关调用交互
 */
@RestController
@RequestMapping(value = "/marketing/linkgo")
@Tag(name = "短链相关调用", description = "短链相关调用")
public class MarketingLinkGoController {


    @Autowired
    private ICustomerConfigService customerConfigService;

    @Operation(summary = "获取3k的加密方式")
    @PostMapping("/getThreeKeyEncryptType")
    public ApiResult<Integer> getThreeKeyEncryptType(@RequestBody CustomerEncryptDTO dto) {
        Result<Integer> result = customerConfigService.getEncryptyType(dto.getApiCode());
        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            return new ApiResult<Integer>().success(result.getData());
        } else {
            return new ApiResult<Integer>().fail(result.getMessage());
        }

    }


}
