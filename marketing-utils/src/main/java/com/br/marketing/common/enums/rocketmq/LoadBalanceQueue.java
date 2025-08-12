package com.br.marketing.common.enums.rocketmq;

/**
 * 负载均衡消费者接口
 * 用于约束可以进行负载均衡的枚举类应该具备的方法
 */
public interface LoadBalanceQueue {
    /**
     * 获取主题
     * @return 主题
     */
    String getTopic();

    /**
     * 获取标签
     * @return 标签
     */
    String getTag();
}