package com.br.marketing.service;

/**
 * @ClassName CustomFunction
 * @Description 自定义函数接口
 * @Author kongbx
 * @Date 2024/4/22 14:45
 */
public interface CustomFunction {

    /**
     * 自定义函数名
     *
     * @return 自定义函数名
     */
    String functionName();

    /**
     * 最终执行的方法
     *
     * @param param 参数
     * @return 执行结果
     */
    String apply(Object param);
}
