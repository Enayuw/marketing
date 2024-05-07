package com.br.marketing.config;

import com.br.marketing.handle.CustomFunctionFactory;
import com.br.marketing.service.CustomFunction;
import com.br.marketing.service.FunctionService;
import com.br.marketing.service.Impl.DefaultFunctionServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import java.util.List;

/**
 * @ClassName LogAutoConfiguration
 * @Author kongbx
 * @Date 2024/4/22 14:45
 */
@Configuration
public class LogAutoConfiguration {
    @Bean
    @Order(1)
    public CustomFunctionFactory CustomFunctionRegistrar(@Autowired List<CustomFunction> iCustomFunctionList) {
        return new CustomFunctionFactory(iCustomFunctionList);
    }

    @Bean
    @Order(2)
    public FunctionService customFunctionService(CustomFunctionFactory customFunctionFactory) {
        return new DefaultFunctionServiceImpl(customFunctionFactory);
    }
}
