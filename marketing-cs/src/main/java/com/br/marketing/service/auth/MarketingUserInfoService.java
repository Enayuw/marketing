package com.br.marketing.service.auth;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.entity.auth.LoginReqObj;
import com.br.marketing.entity.auth.MarketingUserDetail;

import javax.servlet.http.HttpServletRequest;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 用户接口
 * @Date 2022/3/10 6:21 PM
 * ------------------------------
 */
public interface MarketingUserInfoService {
    /**
     * 用户登录
     * @param reqObj
     * @return 返回成功或者失败
     */
    ApiResult<MarketingUserDetail> login(HttpServletRequest request, LoginReqObj reqObj);

    /**
     * user  logout
     */
    String logOut();





}
