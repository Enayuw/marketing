package com.br.marketing.aspect;
import com.br.marketing.context.ThreadApicodeInfo;
import com.br.marketing.context.ThreadContextInfo;
import com.br.marketing.entity.auth.MarketingUserDetail;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 用户权限拦截器
 */
@Component
@Aspect
public class AuthPermissionsAspect {

    /**
     * 方法
     *
     * @param
     * @return
     */
    @Pointcut("@annotation(com.br.marketing.mysqlInterceptor.AddDataAuthBusiness)")
    public void pointCut() {

    }

    /**
     * 前置调用
     *
     * @param
     * @return
     */
    @Before("pointCut()")
    public void before() {
        MarketingUserDetail user = ThreadContextInfo.getUser();
        if (user != null) {
            List adminUser = user.getRoleList().stream().filter(marketingRole -> marketingRole.getId() == 1).collect(Collectors.toList());
            //超级管理员 跳过
            if (!CollectionUtils.isEmpty(adminUser)) {
                return;
            }
            ThreadApicodeInfo.setData(user.getApiCode());
        }
    }

    /**
     * 后置调用
     *
     * @param
     * @return
     */
    @After("pointCut()")
    public void after() {
        ThreadApicodeInfo.removeData();
    }
}
