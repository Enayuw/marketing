package com.br.marketing.service.Impl.auth;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.auth.AuthConstants;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.entity.Marketing;
import com.br.marketing.entity.MarketingUser;
import com.br.marketing.entity.auth.*;
import com.br.marketing.mapper.auth.MarketingUserInfoMapper;
import com.br.marketing.mapper.auth.MarketingUserInfoRoleMapper;
import com.br.marketing.service.auth.MarketingUserInfoService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.RegEx;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 用户接口实现类
 * @Date 2022/3/11 9:48 AM
 * ------------------------------
 */
@Service
public class MarketingUserInfoServiceImpl implements MarketingUserInfoService {

    @Resource
    private RedisAuthService redisAuthService;

    @Resource
    private MarketingUserInfoMapper marketingUserInfoMapper;

    @Resource
    private MarketingUserInfoRoleMapper marketingUserInfoRoleMapper;

    @Override
    public ApiResult<MarketingUserDetail> login(HttpServletRequest request, LoginReqObj reqObj) {
        if (checkParam(reqObj)) {
            if (kapError(reqObj)) {
                return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_CHECK_CODE_ERROR);
            }
            MarketingUserInfoExample marketingUserInfoExample = new MarketingUserInfoExample();
            marketingUserInfoExample.createCriteria().andUserNameEqualTo(reqObj.getUsername()).andStatusEqualTo(1);
            MarketingUserInfo marketingUserInfo = marketingUserInfoMapper.selectUserInfo(marketingUserInfoExample);
            if (pwdError(reqObj, marketingUserInfo)) {
                return new ApiResult<MarketingUserDetail>().fail().fail(ServiceResultEnum.AUTH_LOGIN_PASS_ERROR);
            }
            // 查询当前用户所有角色
            List<MarketingRole> marketingRoles = marketingUserInfoRoleMapper.getRolesByUid(marketingUserInfo.getId());
            // 查询当前角色的资源
            List<MarketingResource> marketingResources = marketingUserInfoRoleMapper.getResourcesByUid(marketingUserInfo.getId());
            MarketingUserDetail marketingUserDetail = new MarketingUserDetail(marketingUserInfo, marketingRoles, marketingResources, new HashMap<>());
            marketingUserDetail.setSessionId(reqObj.getSessionid());
            marketingUserDetail.setPassword(null);
            request.getSession().setAttribute(AuthConstants.SESSION_USER, marketingUserDetail);
            redisAuthService.set(reqObj.getSessionid(), JSON.toJSONString(marketingUserDetail), AuthConstants.SESSION_FLAG);
            //过期时间
            redisAuthService.expire(reqObj.getSessionid(), AuthConstants.SESSION_FLAG, 30 * 60);
            return new ApiResult<MarketingUserDetail>().success(marketingUserDetail);
        }
        return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);

    }

    /**
     * 密码校验
     *
     * @param reqObj
     * @param user
     * @return
     */
    private boolean pwdError(LoginReqObj reqObj, MarketingUserInfo user) {
        String secPass = getSecPass(user.getUserName(), user.getPassword(), reqObj.getCaptcha());
        return !secPass.equals(reqObj.getPassword());
    }

    /**
     * md5转换
     *
     * @param username
     * @param password
     * @param captcha  验证码
     * @return
     */
    private static String getSecPass(String username, String password, String captcha) {
        return md5(md5(username + password) + captcha);
    }

    /**
     * md5
     *
     * @param str
     * @return
     */
    private static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }

    @Override
    public String logOut() {
        return null;
    }

    /**
     * 空校验
     *
     * @param reqObj
     * @return
     */
    private boolean checkParam(LoginReqObj reqObj) {
        return StringUtils.isNotBlank(reqObj.getUsername()) && StringUtils.isNotBlank(reqObj.getPassword())
                && StringUtils.isNotBlank(reqObj.getCaptcha()) && StringUtils.isNotBlank(reqObj.getSessionid());
    }

    /**
     * 校验码校验
     *
     * @param reqObj
     * @return
     */
    private boolean kapError(LoginReqObj reqObj) {
        //得到redis中框架生成的验证码
        String kaptchaExpected = redisAuthService.get(reqObj.getSessionid(), AuthConstants.SESSION_CAPTCHA);
        //校验验证码是否正确
        if (!reqObj.getCaptcha().equals(StringUtils.isNotBlank(kaptchaExpected) ? kaptchaExpected : "")) {
            redisAuthService.del(reqObj.getSessionid(), AuthConstants.SESSION_CAPTCHA);
            return true;
        }
        return false;
    }
}
