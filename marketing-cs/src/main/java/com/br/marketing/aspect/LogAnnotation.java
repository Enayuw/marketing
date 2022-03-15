package com.br.marketing.aspect;

import java.lang.annotation.*;

/**
 * 切面
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/3 15:06
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/3 15:06
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogAnnotation {
}
