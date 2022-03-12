package com.br.marketing.innerapi.controller.auth;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.entity.auth.LoginReqObj;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.service.auth.MarketingUserInfoService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 权限控制器
 * @Date 2022/3/10 11:37 AM
 * ------------------------------
 */
@RestController
@RequestMapping("auth")
public class MarketingAuthController {

    @Resource
    private MarketingUserInfoService marketingUserInfoService;
    @PostMapping(value = "/login")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<MarketingUserDetail> login(HttpServletRequest request, LoginReqObj reqObj) {
        return marketingUserInfoService.login(request,reqObj);

    }
}
