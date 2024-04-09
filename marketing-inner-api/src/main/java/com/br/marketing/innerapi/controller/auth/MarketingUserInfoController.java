package com.br.marketing.innerapi.controller.auth;

import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.MarketingRole;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.entity.auth.MarketingUserInfo;
import com.br.marketing.entity.auth.PasswordReq;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.mysqlInterceptor.AddDataAuthBusiness;
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
     */
    @GetMapping("/getAllRole")
    public ApiResult<List<MarketingRole>> getAllRole() {
        List<MarketingRole> marketingRoles = marketingRoleService.selectRoleList();
        return new ApiResult<List<MarketingRole>>().success(marketingRoles);
    }

    /**
     * 查看用户列表
     */
    @GetMapping("/list")
    public ApiResult<PageResultReturn>  list(String key, Integer current, Integer size) {
        PageResultReturn listPage = marketingUserInfoService.selectList(key, current, size);
        if (listPage != null) {
            return new ApiResult<PageResultReturn>().success(listPage);
        }
        return new ApiResult<PageResultReturn>().fail(ServiceResultEnum.FAILED);
    }

    /**
     * 保存用户
     *
     */
    @GetMapping("/save")
    public ApiResult<Boolean> insert(HttpServletRequest request, MarketingUserInfo user) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        return marketingUserInfoService.save(userDetail, user);
    }

    /**
     * 校验用户名是否存在
     *
     */
    @GetMapping("/checkName")
    public ApiResult<Boolean> checkName(String username) {
        return new ApiResult<Boolean>().success(marketingUserInfoService.checkUserName(username));
    }

    /**
     * 删除用户
     *
     */
    @GetMapping("/delete")
    public ApiResult<Boolean> delete(String ids) {
        return marketingUserInfoService.delete(ids);
    }

    /**
     * 更新用户
     *
     */
    @GetMapping("/update")
    public ApiResult<Boolean> update(HttpServletRequest request, MarketingUserInfo user) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        return marketingUserInfoService.updateMarketingUserInfo(userDetail, user);
    }

    /**
     * 获取用户信息
     *
     */
    @GetMapping("/getById")
    public ApiResult<MarketingUserInfo> getUserById(Integer id) {
        return new ApiResult<MarketingUserInfo>().success(marketingUserInfoService.getById(id));
    }

    /**
     * 校验旧密码
     *
     */
    @PostMapping("/ajaxCheckOldPwd")
    public ApiResult<Boolean> ajaxCheckOldPwd(HttpServletRequest request, PasswordReq passwordReq) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
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
     */
    @PostMapping("/updatePassword")
    public ApiResult<Boolean> updatePassword(HttpServletRequest request, PasswordReq passwordReq) {
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
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

    /**
     * 更新用户
     *
     */
    @GetMapping("/updateByUserId")
    public ApiResult<Boolean> updateByUserId(HttpServletRequest request, MarketingUserInfo user) {
        return marketingUserInfoService.updateMarketingUserInfoApiCodes( user);
    }
}

