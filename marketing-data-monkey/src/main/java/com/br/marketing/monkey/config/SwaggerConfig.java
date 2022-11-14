package com.br.marketing.monkey.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * swagger配置
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    private static final Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

    @Value("${spring.profiles.active}")
    String proAction;

    /**
     * 生成接口文档方法
     *
     * @return
     */
    @Bean
    public Docket apiConfig() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("com.br.marketing.monkey.controller"))
                .paths((String input) -> {
                    if ("prod".equals(proAction.toLowerCase())) {
                        return false;
                    } else {
                        return true;
                    }
                })
                .build();

        return docket;
    }

    @SuppressWarnings("deprecation")
    private ApiInfo apiInfo() {
        String ip = "";
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            if (log.isErrorEnabled()) {
                log.error(e.getMessage(), e);
            }
            e.printStackTrace();
        }
        if (address != null) {
            ip = address.getHostName() + "-" + address.getCanonicalHostName();
        }
//        ApiInfo apiInfo = new ApiInfo("营销平台内部api", "此在线API手册为调用营销平台技术人员提供开发参考", "1.0.0", "Terms of service", new Contact("百融云. 服务器信息: " + ip, "", ""),
//                "百融云", "https://www.brgroup.com/");
        return new ApiInfoBuilder()
                .title("营销平台内部api")
                .description("此在线API手册为调用营销平台技术人员提供开发参考")
                .version("1.0.0")
                .termsOfServiceUrl("Terms of service")
                .contact(new Contact("百融云. 服务器信息: " + ip, "", ""))
                .license("百融云")
                .licenseUrl("https://www.brgroup.com/")
                .build();
    }
}
