package com.br.marketing.es.util;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Properties;

/**
 * 配置文件处理工具类
 *
 * @Author linquan.guo
 * @CreateDate 2020/12/29 15:47
 * @UpdateUser linquan.guo
 * @UpdateDate 2020/12/29 15:47
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Slf4j
public class PropertiesUtil {
    private PropertiesUtil() {
        throw new IllegalStateException("Utility class");
    }

    private static Properties properties;

    static {
        properties = new Properties();
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties")) {
            properties.load(in);
        } catch (Exception e) {
            log.warn("Exception", e);
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static Integer getIntegerValue(String key) {
        return Integer.valueOf(properties.getProperty(key));
    }

    public static long getLongValue(String key) {
        return Long.parseLong(properties.getProperty(key));
    }

    public static String getStringValue(String key) {
        return properties.getProperty(key);
    }

    public static boolean getBooleanValue(String key) {
        return Boolean.parseBoolean(properties.getProperty(key));
    }

    public static Object getValue(String key) {
        return properties.getProperty(key);
    }

}
