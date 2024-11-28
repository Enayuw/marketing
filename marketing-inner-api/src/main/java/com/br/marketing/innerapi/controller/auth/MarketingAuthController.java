package com.br.marketing.innerapi.controller.auth;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.auth.LoginReqObj;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.entity.auth.MarketingUserInfo;
import com.br.marketing.entity.auth.PasswordReq;
import com.br.marketing.service.auth.MarketingUserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
    public ApiResult<MarketingUserDetail> login(HttpServletRequest request, LoginReqObj reqObj) {
        return marketingUserInfoService.login(request,reqObj);
    }


    @PostMapping(value = "/loginAutoTest")
    public ApiResult<MarketingUserDetail> loginAutoTest(HttpSession session, HttpServletRequest request, LoginReqObj reqObj) {
        return marketingUserInfoService.loginAutoTest(session,request,reqObj);
    }

    @PostMapping("/updatePassword")
    public ApiResult<Boolean> updatePassword(HttpServletRequest request, PasswordReq passwordReq) {
        return  marketingUserInfoService.updatePassword(passwordReq);


    }
    /**
     * 获取用户信息
     *
     */
    @GetMapping
    public ApiResult<MarketingUserDetail> auth(HttpServletRequest request) {
      return marketingUserInfoService.auth(request);
    }
    /**
     * 登出
     *
     */
    @GetMapping(value = "/loginOut")
    public ApiResult<Boolean> loginOut(HttpServletRequest request) {
       return  marketingUserInfoService.logOut(request);
    }
}
