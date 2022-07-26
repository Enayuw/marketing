package com.br.marketing.mysqlInterceptor;


import java.lang.annotation.*;

/**
 * 数据权限自定义注解
 * --------------------------------
 *
 * @BelongsProject: IntelliJ IDEA
 * @BelongsPackage: com.br.marketing.mysqlInterceptor
 * @Description: 数据权限自定义注解
 * @CreateTime: 2022-07-08 14 :31
 * @Version: 1.0
 * @Author: guangchao.zhang
 * ------------------------------
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AddDataAuthBusiness {
    String value() default "";
}
