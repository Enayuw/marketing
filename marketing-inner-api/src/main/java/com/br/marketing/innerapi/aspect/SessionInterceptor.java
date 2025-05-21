package com.br.marketing.innerapi.aspect;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.context.OptUser;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.auth.MarketingUserDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SessionInterceptor  extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

    @Autowired
    private RedisAuthService redisService;

    @Autowired
    OptUser optUser;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionId = request.getParameter("sessionId");
        if (!StringUtils.hasText(sessionId)) {
            sessionId = request.getHeader("sessionId");
        }
        if (sessionId != null) {
            MarketingUserDetail userDetail = this.getCacheAuthUser(sessionId, request);
            if (userDetail == null) {
                checkFailedResult(response);
                return false;
            } else {
                ThreadContextInfo.setUser(userDetail);
                optUser.setUserDetail(userDetail);
                return super.preHandle(request, response, handler);
            }
        } else {
            log.warn("【session校验失败】sessionId 为空");
            checkFailedResult(response);
            return false;
        }
    }

    private void checkFailedResult(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(JSON.toJSONBytes(new ApiResult<>().fail(CodeEnum.USER_INVALID_SESSION_ERROR.getCode()
                , CodeEnum.USER_INVALID_SESSION_ERROR.getMessage())));
        outputStream.close();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object arg2, Exception arg3) throws Exception {
        ThreadContextInfo.removeUser();
    }

    private MarketingUserDetail getCacheAuthUser(String sessionId, HttpServletRequest request) {
        if (this.redisService == null) {
            BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            this.redisService = (RedisAuthService) factory.getBean("redisService");
        }

        String result = this.redisService.get(sessionId, "app_session_prefix");
        if (!StringUtils.hasText(result)) {
            log.warn("【session校验失败】获取到用户信息为空");
            return null;
        } else {
            MarketingUserDetail userDetail = (MarketingUserDetail) JSON.parseObject(result, MarketingUserDetail.class);
            this.redisService.expire(sessionId, "app_session_prefix", 1800);
            return userDetail;
        }
    }
}
