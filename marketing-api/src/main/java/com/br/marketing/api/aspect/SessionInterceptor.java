package com.br.marketing.api.aspect;

import com.alibaba.fastjson.JSON;
import com.br.marketing.client.RedisAuthService;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.common.exception.auth.AppException;
import com.br.marketing.entity.auth.MarketingUserDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Configuration
public class SessionInterceptor  extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(SessionInterceptor.class);

    @Autowired
    private RedisAuthService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String sessionId = request.getParameter("sessionId");
        if (!StringUtils.hasText(sessionId)) {
            sessionId = request.getHeader("sessionId");
        }

        HttpSession session = request.getSession();
        if (sessionId != null) {
            session.setAttribute("sessionId", sessionId);
            MarketingUserDetail userDetail = this.getCacheAuthUser(sessionId, request);
            if (userDetail == null) {
                session.invalidate();
                throw new AppException(CodeEnum.USER_INVALID_SESSION_ERROR);
            } else {
                session.setAttribute("userDetail", userDetail);
                return super.preHandle(request, response, handler);
            }
        } else {
            log.warn("【session校验失败】sessionId 为空");
            throw new AppException(CodeEnum.USER_INVALID_SESSION_ERROR);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object arg2, Exception arg3) throws Exception {
        HttpSession session = request.getSession();
        response.setHeader("sessionId", (String)session.getAttribute("sessionId"));
        session.invalidate();
        session = null;
    }

    private MarketingUserDetail getCacheAuthUser(String sessionId, HttpServletRequest request) {
        if (this.redisService == null) {
            BeanFactory factory = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());
            this.redisService = (RedisAuthService)factory.getBean("redisService");
        }

        String result = this.redisService.get(sessionId, "app_session_prefix");
        if (!StringUtils.hasText(result)) {
            request.getSession().invalidate();
            log.warn("【session校验失败】获取到用户信息为空");
            throw new AppException(CodeEnum.USER_INVALID_SESSION_ERROR);
        } else {
            MarketingUserDetail userDetail = (MarketingUserDetail) JSON.parseObject(result, MarketingUserDetail.class);
            this.redisService.expire(sessionId, "app_session_prefix", 1800);
            return userDetail;
        }
    }
}
