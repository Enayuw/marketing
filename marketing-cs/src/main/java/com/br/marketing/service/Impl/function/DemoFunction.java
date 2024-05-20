package com.br.marketing.service.Impl.function;

import com.br.marketing.enums.FunctionNameEnum;
import com.br.marketing.service.CustomFunction;
import org.springframework.stereotype.Component;

/**
 * @ClassName DemoFunction
 * @Description 演示demo 可在apply中执行具体逻辑
 * @Author kongbx
 * @Date 2024/4/22 16:10
 */
@Component
public class DemoFunction implements CustomFunction {
    @Override
    public String functionName() {
        return FunctionNameEnum.GET_USER_NAME.getName();
    }

    @Override
    public String apply(Object param) {
        return param.toString();
    }
}
