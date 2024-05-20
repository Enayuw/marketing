package com.br.marketing.service;

/**
 * @ClassName FunctionService
 * @Author kongbx
 * @Date 2024/4/22 14:45
 */
public interface FunctionService {

    /**
     * 执行函数
     *
     * @param functionName 函数名
     * @param value        参数
     * @return 执行结果
     */
    String apply(String functionName, Object value);

    /**
     * 是否在拦截的方法执行前执行
     *
     * @param functionName 函数名
     * @return
     */
    boolean executeBefore(String functionName);
}
