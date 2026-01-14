package com.br.marketing.monkey.config;

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

    @Bean
    public OpenAPI customOpenAPI() {

        String activeProfile = environment.getActiveProfiles()[0];

        OpenAPI info = new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("bearer-key",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .info(new Info()
                        .title("营销平台数据猴子api")
                        .description("此在线API手册为调用营销平台技术人员提供开发参考")
                        .version("v1.0.0")
                        .license(new License().name("百融云").url("https://brgroup.com")));

        if(!"dev".equals(activeProfile)){
            info.addServersItem(new Server().url("/api/marketing-data-monkey"));
        }

        return info;
    }
}
