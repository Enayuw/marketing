package com.br.marketing.innerapi.aspect;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.auth.CodeEnum;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.auth.MarketingResource;
import com.br.marketing.entity.auth.MarketingUserDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        MarketingUserDetail userDetail = ThreadContextInfo.getUser();
        List<MarketingResource> resourcesList = userDetail.getResourcesList();
        boolean flag = false;
        Iterator var9 = resourcesList.iterator();

        while(var9.hasNext()) {
            MarketingResource r = (MarketingResource)var9.next();
            if (!StringUtils.isEmpty(r.getAuthority()) && path.matches(r.getAuthority())) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            log.info("【权限校验失败】code: {},message: {},path: {},Resources: {}", new Object[]{CodeEnum.USER_NOTRESOURCES_ERROR.getCode(), CodeEnum.USER_NOTRESOURCES_ERROR.getMessage(), path, JSONObject.toJSONString(resourcesList)});
            response.setHeader("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(JSON.toJSONBytes(new ApiResult<>().fail(CodeEnum.USER_NOTRESOURCES_ERROR.getCode()
                    , CodeEnum.USER_NOTRESOURCES_ERROR.getMessage())));
            outputStream.close();
            return false;
        } else {
            return super.preHandle(request, response, handler);
        }
    }
}
