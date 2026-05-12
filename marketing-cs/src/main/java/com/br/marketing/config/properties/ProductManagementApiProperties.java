package com.br.marketing.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 与配置 {@code api.product-management} 对应（url、productTypeUrl）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "api.product-management")
public class ProductManagementApiProperties {

    private String url;
    private String productTypeUrl;
}
