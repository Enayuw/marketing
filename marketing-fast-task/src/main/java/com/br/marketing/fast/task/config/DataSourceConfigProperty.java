package com.br.marketing.fast.task.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="datasource")
public class DataSourceConfigProperty {
}
