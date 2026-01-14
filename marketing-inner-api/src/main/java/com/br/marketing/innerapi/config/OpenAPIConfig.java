package com.br.marketing.innerapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.Resource;

/**
 * OpenAPI 配置
 */
@Configuration
public class OpenAPIConfig {

    @Resource
    Environment environment;

    /**
     * 自定义 OpenAPI 配置
     * 配置 Swagger/OpenAPI 文档的基本信息，包括安全方案、API 信息、许可证等
     * 根据当前激活的环境配置（非开发环境）添加服务器 URL
     *
     * @return OpenAPI 配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {

        String activeProfile = environment.getActiveProfiles()[0];

        OpenAPI info = new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .info(new Info()
                        .title("营销平台内部api")
                        .description("此在线API手册为调用营销平台技术人员提供开发参考")
                        .version("v1.0.0")
                        .license(new License().name("百融云").url("https://brgroup.com")));

        if(!"dev".equals(activeProfile)){
            info.addServersItem(new Server().url("/api/marketing-inner-api"));
        }

        return info;
    }
}
