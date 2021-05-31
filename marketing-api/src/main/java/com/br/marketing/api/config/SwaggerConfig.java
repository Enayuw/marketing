package com.br.marketing.api.config;




import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${spring.profiles.active}")
    String ProAction;

    @Bean
    public Docket apiConfig() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo()).select()
                .apis(RequestHandlerSelectors.basePackage("com.br.marketing.api.controller"))
                .paths(input -> {
                    if (ProAction.toLowerCase().equals("prod")){
                        return false;
                    }else{
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
            e.printStackTrace();
        }
        if (address != null) {
            ip = address.getHostName() + "-" + address.getCanonicalHostName();
        }
        ApiInfo apiInfo = new ApiInfo("营销平台api", "此在线API手册为调用营销平台技术人员提供开发参考", "1.0.0", "Terms of service", new Contact("百融云. 服务器信息: " + ip, "", ""),
                "百融云", "https://www.brgroup.com/");
        return apiInfo;
    }
}
