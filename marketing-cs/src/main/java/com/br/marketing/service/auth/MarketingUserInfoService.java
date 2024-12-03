package com.br.marketing.service.auth;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.LoginReqObj;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.entity.auth.MarketingUserInfo;
import com.br.marketing.entity.auth.PasswordReq;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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
     * @param request 请求流
     * @param reqObj 用户入参
     * @return 返回用户登录情况 session + MarketingUserDetail
     */
    ApiResult<MarketingUserDetail> login(HttpServletRequest request, LoginReqObj reqObj);

    /**
     * 用户登录
     * @param request 请求流
     * @param reqObj 用户入参
     * @return 返回用户登录情况 session + MarketingUserDetail
     */
    ApiResult<MarketingUserDetail> loginAutoTest(HttpSession httpSession, HttpServletRequest request, LoginReqObj reqObj);
    /**
     * 用户退出
     * @param request 请求流
     * @return 返回登出的信息
     */
    ApiResult<Boolean> logOut(HttpServletRequest request);


    /**
     * 根据session 获取用户信息
     * @param request 请求流
     * @return 返回
     */
    ApiResult<MarketingUserDetail> auth(HttpServletRequest request);

    /**
     * 获取用户列表
     * @param key 搜索关键名字
     * @param pageNo 分页数
     * @param pageSize 每页数量
     * @return 返回用户列表
     */
    PageResultReturn selectList(String key, Integer pageNo, Integer pageSize);

    ApiResult<Boolean> save(MarketingUserDetail userDetail, MarketingUserInfo user);

    /**
     * @param username
     * @return
     */
    boolean checkUserName(String username);

    ApiResult<Boolean> delete(String ids);

    ApiResult<Boolean> updateMarketingUserInfo(MarketingUserDetail userDetail, MarketingUserInfo user);

    MarketingUserInfo selectById(MarketingUserDetail userDetail);

    ApiResult<Boolean> updateMarketingUserPassword(MarketingUserInfo marketingUserInfo);

    MarketingUserInfo getById(Integer id);

    ApiResult<Boolean> updateMarketingUserInfoApiCodes(MarketingUserInfo marketingUserInfo);


    ApiResult<Boolean> updatePassword(PasswordReq passwordReq);
}
