package com.br.marketing.service.Impl;

import com.br.marketing.handle.CustomFunctionFactory;
import com.br.marketing.service.CustomFunction;
import com.br.marketing.service.FunctionService;

/**
 * @ClassName DefaultFunctionServiceImpl
 * @Description 自定义函数的默认实现，增加一层是为了屏蔽底层与上层直接接触
 * @Author kongbx
 * @Date 2024/4/22 14:45
 */
public class DefaultFunctionServiceImpl implements FunctionService {

    private final CustomFunctionFactory customFunctionFactory;

    public DefaultFunctionServiceImpl(CustomFunctionFactory customFunctionFactory) {
        this.customFunctionFactory = customFunctionFactory;
    }

    @Override
    public String apply(String functionName, Object value) {
        CustomFunction function = customFunctionFactory.getFunction(functionName);
        if (function == null) {
            return value.toString();
        }
        return function.apply(value);
    }

    @Override
    public boolean executeBefore(String functionName) {
        CustomFunction function = customFunctionFactory.getFunction(functionName);
        return function != null;
    }
}
