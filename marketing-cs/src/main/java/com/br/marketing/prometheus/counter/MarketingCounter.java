package com.br.marketing.prometheus.counter;

import io.prometheus.client.Gauge;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MarketingCounter implements ImportBeanDefinitionRegistrar {
    private static Map<String, Gauge> counterMap = new ConcurrentHashMap();
    private static boolean initialized = false;
    private static String NAME_SPACE;


    public MarketingCounter() {
    }


    public static void count(String metricName, String value) {
        Assert.notNull(metricName, "指标名metricName不能为null");
        Assert.notNull(value, "指标值value不能为null");
        String counterKey = NAME_SPACE + "@" + metricName;
        if (counterMap.get(counterKey) == null) {
            synchronized (MarketingCounter.class) {
                if (counterMap.get(counterKey) == null) {
                    Gauge gauge = Gauge.build().namespace(NAME_SPACE)
                            .name(metricName)
                            .help(metricName)
                            .labelNames(new String[]{"value"})
                            .register();
                    counterMap.put(counterKey, gauge);
                    (gauge.labels(value)).inc();
                } else {
                    ((counterMap.get(counterKey)).labels(value)).inc();
                }
            }
        } else {
            (counterMap.get(counterKey)).labels(value).inc();
        }

    }

    public static void countDec(String metricName, String value) {
        Assert.notNull(metricName, "指标名metricName不能为null");
        Assert.notNull(value, "指标值value不能为null");
        String counterKey = NAME_SPACE + "@" + metricName;
        if (counterMap.get(counterKey) == null) {
            synchronized (MarketingCounter.class) {
                if (counterMap.get(counterKey) == null) {
                    Gauge gauge = Gauge.build().namespace(NAME_SPACE)
                            .name(metricName)
                            .help(metricName)
                            .labelNames(new String[]{"value"})
                            .register();
                    counterMap.put(counterKey, gauge);
                    (gauge.labels(value)).dec();
                } else {
                    ((counterMap.get(counterKey)).labels(value)).dec();
                }
            }
        } else {
            (counterMap.get(counterKey)).labels(value).dec();
        }

    }

    public static void count(String metricName, String label, String value) {
        Assert.notNull(metricName, "指标名metricName不能为null");
        Assert.notNull(label, "指标子类型标签label不能为null");
        Assert.notNull(value, "指标值value不能为null");
        String counterKey = NAME_SPACE + "@" + metricName;
        if (counterMap.get(counterKey) == null) {
            synchronized (MarketingCounter.class) {
                if (counterMap.get(counterKey) == null) {
                    Gauge gauge = Gauge.build().namespace(NAME_SPACE)
                            .name(metricName)
                            .help(metricName)
                            .labelNames(new String[]{"metric_label", "value"})
                            .register();
                    counterMap.put(counterKey, gauge);
                    (gauge.labels(label, value)).inc();
                } else {
                    counterMap.get(counterKey).labels(label, value).inc();
                }
            }
        } else {
            ((counterMap.get(counterKey)).labels(label, value)).inc();
        }

    }

    public static void countDec(String metricName, String label, String value) {
        Assert.notNull(metricName, "指标名metricName不能为null");
        Assert.notNull(label, "指标子类型标签label不能为null");
        Assert.notNull(value, "指标值value不能为null");
        String counterKey = NAME_SPACE + "@" + metricName;
        if (counterMap.get(counterKey) == null) {
            synchronized (MarketingCounter.class) {
                if (counterMap.get(counterKey) == null) {
                    Gauge gauge = Gauge.build().namespace(NAME_SPACE)
                            .name(metricName)
                            .help(metricName)
                            .labelNames(new String[]{"metric_label", "value"})
                            .register();
                    counterMap.put(counterKey, gauge);
                    (gauge.labels(label, value)).dec();
                } else {
                    counterMap.get(counterKey).labels(label, value).dec();
                }
            }
        } else {
            ((counterMap.get(counterKey)).labels(label, value)).dec();
        }

    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        Class var3 =MarketingCounter.class;
        synchronized (MarketingCounter.class) {
            if (!initialized) {
                NAME_SPACE = (String) annotationMetadata.getAnnotationAttributes(EnableMarketingCounter.class.getName()).get("namespace");
                if (NAME_SPACE.startsWith("${") && NAME_SPACE.endsWith("}")) {
                    NAME_SPACE = System.getenv(NAME_SPACE.substring(2, NAME_SPACE.length() - 1));
                }
                initialized = true;
            }

        }
    }
}
