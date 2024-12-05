package com.br.marketing.tools;

import io.shardingsphere.shardingjdbc.spring.boot.SpringBootConfiguration;
import org.springframework.boot.SpringApplication;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.MultipartAutoConfiguration;

@SpringBootApplication(exclude = {MultipartAutoConfiguration.class,SpringBootConfiguration.class})
public class ToolsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ToolsApplication.class);
    }
}
