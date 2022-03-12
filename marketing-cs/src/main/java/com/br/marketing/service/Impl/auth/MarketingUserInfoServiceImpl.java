package com.br.marketing.service.Impl.auth;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.auth.AuthConstants;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.entity.auth.*;
import com.br.marketing.mapper.auth.MarketingUserInfoMapper;
import com.br.marketing.mapper.auth.MarketingUserInfoRoleMapper;
import com.br.marketing.service.auth.MarketingUserInfoService;
import com.github.pagehelper.PageHelper;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


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
            MarketingUserDetail marketingUserDetail = new MarketingUserDetail(marketingUserInfo, marketingRoles, marketingResources, new HashMap<>(16));
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

    @Override
    public ApiResult<Boolean> logOut(HttpServletRequest request) {
        String sessionId = request.getHeader("sessionId");
        //清除session的所有信息
        request.getSession().invalidate();
        if (StringUtils.isNotBlank(sessionId)) {
            redisAuthService.del(sessionId, AuthConstants.SESSION_FLAG);
        }
        return new ApiResult<Boolean>().success(ServiceResultEnum.SUCCESS);
    }

    /**
     * 密码校验
     */
    private boolean pwdError(LoginReqObj reqObj, MarketingUserInfo user) {
        String secPass = getSecPass(user.getUserName(), user.getPassword(), reqObj.getCaptcha());
        return !secPass.equals(reqObj.getPassword());
    }

    /**
     * md5转换
     */
    private static String getSecPass(String username, String password, String captcha) {
        return md5(md5(username + password) + captcha);
    }

    /**
     * md5
     */
    private static String md5(String str) {
        return DigestUtils.md5Hex(str);
    }


    @Override
    public ApiResult<MarketingUserDetail> auth(HttpServletRequest request) {
        String sessionId = request.getHeader("sessionId");
        if (StringUtils.isNotBlank(sessionId)) {
            String userMsg = redisAuthService.get(sessionId, AuthConstants.SESSION_FLAG);
            if (StringUtils.isNotBlank(userMsg)) {
                return new ApiResult<MarketingUserDetail>().success(JSON.parseObject(userMsg, MarketingUserDetail.class));
            } else {
                return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_USER_INVALID_SESSION_ERROR);
            }
        }
        return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);
    }

    @Override
    public ApiResult<List<MarketingUserInfo>> selectList(String key, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        MarketingUserInfoExample marketingUserInfoExample = new MarketingUserInfoExample();
        marketingUserInfoExample.createCriteria().andStatusEqualTo(1).andUserNameLike(key).andRealNameLike(key);
        marketingUserInfoExample.setOrderByClause("create_time");
        marketingUserInfoExample.setOrderByClause("update_time");
        List<MarketingUserInfo> marketingUserInfos = marketingUserInfoMapper.selectByExample(marketingUserInfoExample);
        return new ApiResult<List<MarketingUserInfo>>().success(marketingUserInfos);

    }

    @Override
    public ApiResult<Boolean> save(MarketingUserDetail userDetail, MarketingUserInfo marketingUserInfo) {
        if (StringUtils.isBlank(marketingUserInfo.getUserName())) {
            return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);
        }
        if (!checkUserName(marketingUserInfo.getUserName())) {
            return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_USER_REPEAT);
        }
        marketingUserInfo.setUpdateTime(new Date());
        marketingUserInfo.setCreateTime(new Date());
        marketingUserInfo.setStatus(1);
        marketingUserInfo.setIsDisable(0);
        marketingUserInfo.setCreateUserId(userDetail.getId());
        marketingUserInfo.setCreateUserId(userDetail.getId());
        marketingUserInfoMapper.insert(marketingUserInfo);
        insertUserRole(marketingUserInfo);
        return new ApiResult<Boolean>().success(ServiceResultEnum.SUCCESS);
    }


    private void insertUserRole(MarketingUserInfo user) {
        //创建角色
        String roleIds = user.getRoleIds();
        if (StringUtils.isNotBlank(roleIds)) {
            for (String id : roleIds.split(",")) {
                MarketingUserInfoRole ucUserRole = new MarketingUserInfoRole();
                ucUserRole.setCreateTime(new Date());
                ucUserRole.setUserId(user.getId());
                ucUserRole.setRoleId(Integer.valueOf(id));
                ucUserRole.setUpdateTime(new Date());
                ucUserRole.setStatus(1);
                //保存角色
                marketingUserInfoRoleMapper.insert(ucUserRole);
            }
        }
    }

    /**
     * 查询用户
     *
     * @return true or false
     */
    @Override
    public boolean checkUserName(String username) {
        MarketingUserInfoExample marketingUserInfoExample = new MarketingUserInfoExample();
        marketingUserInfoExample.createCriteria().andUserNameEqualTo(username).andStatusEqualTo(1);
        List<MarketingUserInfo> marketingUserInfos = marketingUserInfoMapper.selectByExample(marketingUserInfoExample);
        return marketingUserInfos == null || marketingUserInfos.size() <= 0;
    }

    @Override
    public ApiResult<Boolean> delete(String ids) {
        MarketingUserInfoExample marketingUserInfoExample = new MarketingUserInfoExample();
        String[] split = ids.split(",");
        List<Integer> list = new ArrayList<>();
        for (String s : split) {
            list.add(Integer.valueOf(s));
        }
        marketingUserInfoExample.createCriteria().andIdIn(list);
        MarketingUserInfo marketingUserInfo = new MarketingUserInfo();
        marketingUserInfo.setStatus(0);
        marketingUserInfoMapper.updateByExampleSelective(marketingUserInfo, marketingUserInfoExample);
        return new ApiResult<Boolean>().success(ServiceResultEnum.SUCCESS);
    }

    @Override
    public ApiResult<Boolean> updateMarketingUserInfo(MarketingUserDetail userDetail, MarketingUserInfo marketingUserInfo) {
        marketingUserInfo.setUpdateTime(new Date());
        marketingUserInfo.setUpdateUserId(userDetail.getId());
        marketingUserInfoMapper.updateByPrimaryKeySelective(marketingUserInfo);
        //修改角色信息及用户跟几个组之间的关系
        return updateUserRole(marketingUserInfo);
    }

    @Override
    public MarketingUserInfo selectById(MarketingUserDetail userDetail) {
        return marketingUserInfoMapper.selectByPrimaryKey(userDetail.getId());
    }

    @Override
    public ApiResult<Boolean> updateMarketingUserPassword(MarketingUserInfo marketingUserInfo) {
        marketingUserInfoMapper.updateByPrimaryKeySelective(marketingUserInfo);
        return  new ApiResult<Boolean>().success();
    }

    private ApiResult<Boolean> updateUserRole(MarketingUserInfo marketingUserInfo) {
        if (StringUtils.isNotBlank(marketingUserInfo.getRoleIds())) {
            String[] roleId = marketingUserInfo.getRoleIds().split(",");
            //删除原角色
            marketingUserInfoMapper.updateByPrimaryKeySelective(marketingUserInfo);
            //创建新角色
            for (String id : roleId) {
                MarketingUserInfoRole ucUserRole = new MarketingUserInfoRole();
                ucUserRole.setRoleId(Integer.valueOf(id));
                ucUserRole.setUserId(marketingUserInfo.getId());
                ucUserRole.setCreateTime(new Date());
                ucUserRole.setUpdateTime(new Date());
                ucUserRole.setStatus(1);
                //保存角色
                marketingUserInfoRoleMapper.insertSelective(ucUserRole);
            }
            return new ApiResult<Boolean>().success(ServiceResultEnum.SUCCESS);
        }
        return new ApiResult<Boolean>().success(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);

    }

    /**
     * 空校验
     */
    private boolean checkParam(LoginReqObj reqObj) {
        return StringUtils.isNotBlank(reqObj.getUsername()) && StringUtils.isNotBlank(reqObj.getPassword())
                && StringUtils.isNotBlank(reqObj.getCaptcha()) && StringUtils.isNotBlank(reqObj.getSessionid());
    }

    /**
     * 校验码校验
     */
    private boolean kapError(LoginReqObj reqObj) {
        //得到redis中框架生成的验证码
        String captchaExpected = redisAuthService.get(reqObj.getSessionid(), AuthConstants.SESSION_CAPTCHA);
        //校验验证码是否正确
        if (!reqObj.getCaptcha().equals(StringUtils.isNotBlank(captchaExpected) ? captchaExpected : "")) {
            redisAuthService.del(reqObj.getSessionid(), AuthConstants.SESSION_CAPTCHA);
            return true;
        }
        return false;
    }
}
