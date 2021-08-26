package com.br.marketing.api.aspect;


import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.common.exception.auth.AppException;
import com.br.marketing.dto.userinfo.Resources;
import com.br.marketing.dto.userinfo.UserDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.List;

@Configuration
public class AuthInterceptor extends HandlerInterceptorAdapter {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getServletPath().replaceAll("/", ".");
        if (path.substring(0, 1).equalsIgnoreCase(".")) {
            path = path.substring(1);
        }

        log.info("request path =====> {} ", path);
        HttpSession session = request.getSession();
        UserDetail userDetail = (UserDetail)session.getAttribute("userDetail");
        List<Resources> resourcesList = userDetail.getResourcesList();
        boolean flag = false;
        Iterator var9 = resourcesList.iterator();

        while(var9.hasNext()) {
            Resources r = (Resources)var9.next();
            if (!StringUtils.isEmpty(r.getAuthority()) && path.matches(r.getAuthority())) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            log.info("【权限校验失败】code: {},message: {},path: {},Resources: {}", new Object[]{CodeEnum.USER_NOTRESOURCES_ERROR.getCode(), CodeEnum.USER_NOTRESOURCES_ERROR.getMessage(), path, JSONObject.toJSONString(resourcesList)});
            throw new AppException(CodeEnum.USER_NOTRESOURCES_ERROR);
        } else {
            return super.preHandle(request, response, handler);
        }
    }
}
