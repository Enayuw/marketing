package com.br.marketing.innerapi.config;


import com.br.marketing.innerapi.aspect.AuthInterceptor;
import com.br.marketing.innerapi.aspect.SessionInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * webmvc的一些配置
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    AuthInterceptor authInterceptor;

    @Autowired
    SessionInterceptor sessionInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
        registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/rule/**")
                .addPathPatterns("/pushrulefilter/**")
                .addPathPatterns("/user/**")
                .addPathPatterns("/resource/**")
                .addPathPatterns("/transferFile/**")
                .addPathPatterns("/role/**")
                .addPathPatterns("/pushDecisions/**")
                .addPathPatterns("/reportScoreRule/**")
                .addPathPatterns("/bi/report/**")
                .addPathPatterns("/controlGroup/**")
                .addPathPatterns("/xiecheng/**")
                .addPathPatterns("/car/**")
                .addPathPatterns("/ruleCleaning/**")
                .addPathPatterns("/carChannel/**")
                .addPathPatterns("/tag/**")
                .addPathPatterns("/mock/**")
                .addPathPatterns("/account/**")
                .addPathPatterns("/tcCpa/customize/**")
                .addPathPatterns("/template/**")
                .addPathPatterns("/templateJsonParse/**")
                .addPathPatterns("/auto/check/**");
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/rule/**")
                .addPathPatterns("/pushrulefilter/**")
                .addPathPatterns("/user/**")
                .addPathPatterns("/resource/**")
                .addPathPatterns("/transferFile/**")
                .addPathPatterns("/role/**")
                .addPathPatterns("/pushDecisions/**")
                .addPathPatterns("/reportScoreRule/**")
                .addPathPatterns("/bi/report/**")
                .addPathPatterns("/controlGroup/**")
                .addPathPatterns("/xiecheng/**")
                .addPathPatterns("/car/**")
                .addPathPatterns("/ruleCleaning/**")
                .addPathPatterns("/carChannel/**")
                .addPathPatterns("/tag/**")
                .addPathPatterns("/account/**")
                .addPathPatterns("/mock/**")
                .addPathPatterns("/tcCpa/customize/**")
                .addPathPatterns("/template/**")
                .addPathPatterns("/templateJsonParse/**")
                .addPathPatterns("/auto/check/**")
        ;
        super.addInterceptors(registry);
    }
}
