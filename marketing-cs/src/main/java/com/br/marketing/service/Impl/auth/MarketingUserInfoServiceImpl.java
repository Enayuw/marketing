package com.br.marketing.service.Impl.auth;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.br.common.encryption.Sm3Util;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.auth.AuthConstants;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.auth.*;
import com.br.marketing.mapper.auth.MarketingRoleMapper;
import com.br.marketing.mapper.auth.MarketingUserInfoMapper;
import com.br.marketing.mapper.auth.MarketingUserInfoRoleMapper;
import com.br.marketing.service.auth.MarketingUserInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.IpAddressUtil;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;


/**
 * -------------------------------
 *
 * @author guangchao.zhang
 * @Description 用户接口实现类
 * @Date 2022/3/11 9:48 AM
 * ------------------------------
 */
@Slf4j
@Service
public class MarketingUserInfoServiceImpl implements MarketingUserInfoService {

    @Resource
    RedisAuthService redisAuthService;

    @Resource
    private MarketingUserInfoMapper marketingUserInfoMapper;

    @Resource
    private MarketingUserInfoRoleMapper marketingUserInfoRoleMapper;

    @Resource
    private MarketingRoleMapper marketingRoleMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public ApiResult<MarketingUserDetail> login(HttpServletRequest request, LoginReqObj reqObj) {

        if (checkParam(reqObj)) {
            MarketingUserInfoExample marketingUserInfoExample = new MarketingUserInfoExample();
            marketingUserInfoExample.createCriteria().andUserNameEqualTo(reqObj.getUsername()).andStatusEqualTo(1);
            MarketingUserInfo marketingUserInfo = marketingUserInfoMapper.selectUserInfo(marketingUserInfoExample);
            if (kapError(reqObj)) {
                return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_CHECK_CODE_ERROR);
            }
            if (!pwdError(reqObj, marketingUserInfo)) {
                return new ApiResult<MarketingUserDetail>().fail().fail(ServiceResultEnum.AUTH_LOGIN_PASS_ERROR);
            }
            if (marketingUserInfo.getPasswordEditFlag() == 0) {
                return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.EDIT_PASSWORD);
            }
            // 查询当前用户所有角色
            List<MarketingRole> marketingRoles = marketingUserInfoRoleMapper.getRolesByUid(marketingUserInfo.getId());
            // 查询当前角色的资源
            List<MarketingResource> marketingResources = marketingUserInfoRoleMapper.getResourcesByUid(marketingUserInfo.getId());
            MarketingUserDetail marketingUserDetail = new MarketingUserDetail(marketingUserInfo, marketingRoles, marketingResources, new HashMap<>(16));
            marketingUserDetail.setSessionId(reqObj.getSessionId());
            marketingUserDetail.setPassword(null);
            redisAuthService.set(reqObj.getSessionId(), JSON.toJSONString(marketingUserDetail), "app_session_prefix");
            redisAuthService.expire(reqObj.getSessionId(), "app_session_prefix", 1800);
            //过期时间
            return new ApiResult<MarketingUserDetail>().success(marketingUserDetail);
        }
        return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);

    }

    @Override
    public ApiResult<MarketingUserDetail> loginAutoTest(HttpSession httpSession, HttpServletRequest request, LoginReqObj reqObj) {
        log.warn("httpSession:{}",httpSession.getId());
        List<String> ipAddr = IpAddressUtil.getIpAddr(request);
        log.warn("IP:{}",ipAddr);
        List<String> speedTestIp = marketingCommonConfig.getAutoTestIp();
        if(CollectionUtil.containsAny(ipAddr,speedTestIp)){
            MarketingUserInfoExample marketingUserInfoExample = new MarketingUserInfoExample();
            marketingUserInfoExample.createCriteria().andUserNameEqualTo(reqObj.getUsername()).andStatusEqualTo(1);
            MarketingUserInfo marketingUserInfo = marketingUserInfoMapper.selectUserInfo(marketingUserInfoExample);

            // 查询当前用户所有角色
            List<MarketingRole> marketingRoles = marketingUserInfoRoleMapper.getRolesByUid(marketingUserInfo.getId());
            // 查询当前角色的资源
            List<MarketingResource> marketingResources = marketingUserInfoRoleMapper.getResourcesByUid(marketingUserInfo.getId());
            MarketingUserDetail marketingUserDetail = new MarketingUserDetail(marketingUserInfo, marketingRoles, marketingResources, new HashMap<>(16));
            marketingUserDetail.setSessionId(httpSession.getId());
            marketingUserDetail.setPassword(null);
            redisAuthService.set(httpSession.getId(), JSON.toJSONString(marketingUserDetail), "app_session_prefix");
            redisAuthService.expire(httpSession.getId(), "app_session_prefix", 1800);
            //过期时间
            return new ApiResult<MarketingUserDetail>().success(marketingUserDetail);
        }

        return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_LOGIN_NO_PERMISSION);
    }

    @Override
    public ApiResult<Boolean> logOut(HttpServletRequest request) {
        String sessionId = request.getHeader("sessionId");
        if (StringUtils.isNotBlank(sessionId)) {
            redisAuthService.del(sessionId);
        }
        return new ApiResult<Boolean>().success(ServiceResultEnum.SUCCESS);
    }

    /**
     * 密码校验
     */
    private boolean pwdError(LoginReqObj reqObj, MarketingUserInfo user) {
        if (user == null) {
            return false;
        }
        String secPass = getSecPass(user.getUserName(), user.getPassword(), reqObj.getCaptcha());
        String md5SecPass = getMd5SecPass(user.getUserName(), user.getPassword(), reqObj.getCaptcha());
        return secPass.equals(reqObj.getPassword()) || md5SecPass.equals(reqObj.getMd5Password());
    }

    /**
     * md5转换
     */
    private static String getMd5SecPass(String username, String password, String captcha) {
        return md5(md5(username + password) + captcha);
    }

    /**
     * md5转换
     */
    private static String getSecPass(String username, String password, String captcha) {
        try {
            return Sm3Util.getSM3Value(Sm3Util.getSM3Value(username + password).toLowerCase() + captcha).toLowerCase();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            String userMsg = redisAuthService.get(sessionId, "app_session_prefix");
            if (StringUtils.isNotBlank(userMsg)) {
                return new ApiResult<MarketingUserDetail>().success(JSON.parseObject(userMsg, MarketingUserDetail.class));
            } else {
                return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_USER_INVALID_SESSION_ERROR);
            }
        }
        return new ApiResult<MarketingUserDetail>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);
    }

    @Override
    public PageResultReturn selectList(String key, Integer pageNo, Integer pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        Map<String, Object> marketingUserInfo = new HashMap<>();
        if (StringUtils.isNotBlank(key)) {
            marketingUserInfo.put("key", "%" + key + "%");
        }
        List<MarketingUserInfo> marketingUserInfos = marketingUserInfoMapper.selectByExampleList(marketingUserInfo);
        return PageResultReturn.setPageResult(marketingUserInfos, pageNo, pageSize);
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
        return new ApiResult<Boolean>().success();
    }

    @Override
    public ApiResult<Boolean> updateMarketingUserInfoApiCodes(MarketingUserInfo marketingUserInfo) {
        marketingUserInfoMapper.updateByPrimaryKeySelective(marketingUserInfo);
        return new ApiResult<Boolean>().success();
    }

    @Override
    public ApiResult<Boolean> updatePassword(PasswordReq passwordReq) {
        MarketingUserInfoExample marketingUserInfoExample = new MarketingUserInfoExample();
        marketingUserInfoExample.createCriteria().andUserNameEqualTo(passwordReq.getUsername()).andStatusEqualTo(1);
        MarketingUserInfo marketingUserInfo = marketingUserInfoMapper.selectUserInfo(marketingUserInfoExample);
        if (StringUtils.isNotBlank(passwordReq.getNewPassword()) && StringUtils.isNotBlank(passwordReq.getOldPassword())) {
            // 老数据为md5
            if (!passwordReq.getOldPassword().equals(marketingUserInfo.getPassword()) && !passwordReq.getMd5Password().equals(marketingUserInfo.getPassword())) {
                return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_PASSWD_ERROR);
            }
            marketingUserInfo.setPassword(passwordReq.getNewPassword());
            marketingUserInfo.setPasswordEditFlag(1);
            return updateMarketingUserPassword(marketingUserInfo);
        }
        return new ApiResult<Boolean>().fail(ServiceResultEnum.AUTH_FAILED_ERROR_PARAM);
    }


    @Override
    public MarketingUserInfo getById(Integer id) {
        MarketingUserInfo marketingUserInfo = marketingUserInfoMapper.selectByPrimaryKey(id);

        Set<Integer> set = marketingUserInfoRoleMapper.getRoleIds(marketingUserInfo.getId());
        MarketingRoleExample marketingRoleExample = new MarketingRoleExample();
        marketingRoleExample.createCriteria().andStatusEqualTo(1);
        List<MarketingRole> marketingRoles = marketingRoleMapper.selectByExample(marketingRoleExample);

        List<Map<String, Object>> roles = new ArrayList<>();
        for (MarketingRole marketingRole : marketingRoles) {
            Integer roleId = marketingRole.getId();
            String name = marketingRole.getName();
            Map<String, Object> roleMap = new HashMap<>();
            roleMap.put("name", name);
            roleMap.put("id", roleId);
            if (set.contains(roleId)) {
                roleMap.put("select", Boolean.TRUE);
            } else {
                roleMap.put("select", Boolean.FALSE);
            }
            roles.add(roleMap);
        }
        marketingUserInfo.setRoles(roles);
        return marketingUserInfo;
    }

    private ApiResult<Boolean> updateUserRole(MarketingUserInfo marketingUserInfo) {
        if (StringUtils.isNotBlank(marketingUserInfo.getRoleIds())) {
            String[] roleId = marketingUserInfo.getRoleIds().split(",");
            //删除原角色
            MarketingUserInfoRoleExample marketingUserInfoRoleExample = new MarketingUserInfoRoleExample();
            MarketingUserInfoRole marketingUserInfoRole = new MarketingUserInfoRole();
            marketingUserInfoRole.setStatus(0);
            marketingUserInfoRoleExample.createCriteria().andUserIdEqualTo(marketingUserInfo.getId());
            marketingUserInfoRoleMapper.updateByExampleSelective(marketingUserInfoRole, marketingUserInfoRoleExample);
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
                && StringUtils.isNotBlank(reqObj.getCaptcha()) && StringUtils.isNotBlank(reqObj.getSessionId());
    }

    /**
     * 校验码校验
     */
    private boolean kapError(LoginReqObj reqObj) {
        //得到redis中框架生成的验证码
        String captchaExpected = redisAuthService.get(reqObj.getSessionId(), "app_captcha_prefix");
        log.warn("缓存验证码：{}", captchaExpected);
        redisAuthService.del(reqObj.getSessionId(), "app_captcha_prefix");
        //校验验证码是否正确
        return !reqObj.getCaptcha().equals(captchaExpected);
    }
}
