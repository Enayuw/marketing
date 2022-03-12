package com.br.marketing.innerapi.controller.auth;

import cn.hutool.http.server.HttpServerRequest;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.auth.MarketingRole;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.entity.auth.MarketingUserInfo;
import com.br.marketing.entity.auth.PasswordReq;
import com.br.marketing.service.auth.MarketingRoleService;
import com.br.marketing.service.auth.MarketingUserInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 用户控制器
 * @Date 2022/3/10 11:36 AM
 * ------------------------------
 */
@RestController
@RequestMapping("user")
public class MarketingUserInfoController {

    @Resource
    private MarketingUserInfoService marketingUserInfoService;

    @Resource
    private MarketingRoleService marketingRoleService;

    /**
     * 获取所有角色
     *
     * @param
     * @return
     */
    @GetMapping("/getAllRole")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<List<MarketingRole>> getAllRole() {
        List<MarketingRole> marketingRoles = marketingRoleService.selectRoleList();
        return new ApiResult<List<MarketingRole>>().success(marketingRoles);
    }

    /**
     * 查看用户列表
     */
    @GetMapping("/list")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<List<MarketingUserInfo>> list(String key, Integer pageNo, Integer pageSize) {
        return marketingUserInfoService.selectList(key, pageNo, pageSize);
    }

    /**
     * 保存用户
     *
     * @param user
     * @return
     */
    @GetMapping("/save")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> insert(HttpServletRequest request, MarketingUserInfo user) {
        MarketingUserDetail userDetail = (MarketingUserDetail) request.getSession().getAttribute("userDetail");
        return marketingUserInfoService.save(userDetail, user);
    }

    /**
     * 校验用户名是否存在
     *
     * @param username
     * @return
     */
    @GetMapping("/checkName")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> checkName(String username) {
        return new ApiResult<Boolean>().success(marketingUserInfoService.checkUserName(username));
    }

    /**
     * 删除用户
     *
     * @param ids
     * @return
     */
    @GetMapping("/delete")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> delete(String ids) {
        return marketingUserInfoService.delete(ids);
    }

    /**
     * 更新用户
     *
     * @param user
     * @return
     */
    @GetMapping("/update")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> update(HttpServletRequest request, MarketingUserInfo user) {
        MarketingUserDetail userDetail = (MarketingUserDetail) request.getSession().getAttribute("userDetail");
        return marketingUserInfoService.updateMarketingUserInfo(userDetail, user);
    }

    ///**
    // * 获取用户信息
    // *
    // * @param id
    // * @return
    // */
    //@GetMapping("/getById")
    //@PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    //public String getUserById(Integer id) {
    //
    //    //查询用户基本信息
    //    User user = userService.selectOne(new EntityWrapper<User>().eq("id", id));
    //    if (user != null) {
    //        user.setPassword(null);
    //        if (StringUtils.isBlank(user.getEmail())) {
    //            user.setEmail(null);
    //        }
    //        userService.getUserById(user);
    //        return respJson(CodeEnum.SUCC, user);
    //    }
    //    return respJson(CodeEnum.PARAM_ERROR);
    //}

    /**
     * 校验旧密码
     *
     * @param passwordReq
     * @return
     */
    @PostMapping("/ajaxCheckOldPwd")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> ajaxCheckOldPwd(HttpServletRequest request, PasswordReq passwordReq) {
        MarketingUserDetail userDetail = (MarketingUserDetail) request.getSession().getAttribute("userDetail");
        if (StringUtils.isNotBlank(passwordReq.getOldPassword())) {
            MarketingUserInfo marketingUserInfos = marketingUserInfoService.selectById(userDetail);
            if (!passwordReq.getOldPassword().equals(marketingUserInfos.getPassword())) {
                return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_PASSWD_ERROR);
            }
            return new ApiResult<Boolean>().success(ServiceResultEnum.SUCCESS);
        }
        return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);
    }

    /**
     * 修改登录密码
     *
     * @param passwordReq
     * @return
     */
    @PostMapping("/updatePassword")
    @PrometheusTimeMethod(buckets = {0.05d, 0.1d, 0.2d, 0.5d}, methodType = MethodType.ACCESS)
    public ApiResult<Boolean> updatePassword(HttpServletRequest request, PasswordReq passwordReq) {
        MarketingUserDetail userDetail = (MarketingUserDetail) request.getSession().getAttribute("userDetail");
        if (StringUtils.isNotBlank(passwordReq.getNewPassword()) && StringUtils.isNotBlank(passwordReq.getOldPassword())) {
            MarketingUserInfo marketingUserInfo = marketingUserInfoService.selectById(userDetail);
            if (!passwordReq.getOldPassword().equals(marketingUserInfo.getPassword())) {
                return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_PASSWD_ERROR);
            }
            marketingUserInfo.setPassword(passwordReq.getNewPassword());
            return marketingUserInfoService.updateMarketingUserPassword(marketingUserInfo);
        }
        return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);
    }
}

