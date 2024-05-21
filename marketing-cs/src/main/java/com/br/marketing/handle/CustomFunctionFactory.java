package com.br.marketing.handle;

import com.br.marketing.service.CustomFunction;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName CustomFunctionFactory
 * @Description 自定义函数工厂
 * @Author kongbx
 * @Date 2024/4/22 14:45
 */
public class CustomFunctionFactory {

    private static final Map<String, CustomFunction> CUSTOM_FUNCTION_MAPMAP = new ConcurrentHashMap<>();
    

    public CustomFunctionFactory(List<CustomFunction> customFunctions) {
        for (CustomFunction customFunction : customFunctions) {
            CUSTOM_FUNCTION_MAPMAP.put(customFunction.functionName(), customFunction);
        }
    }
    
    /**
     * 通过函数名获取对应自定义函数
     *
     * @param functionName 函数名
     * @return 自定义函数
     */
    public CustomFunction getFunction(String functionName) {
        return CUSTOM_FUNCTION_MAPMAP.get(functionName);
    }

}
