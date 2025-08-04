package com.br.marketing.service.Impl.ai;

import com.br.marketing.client.RedisChgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * 队列负载均衡器
 * 基于Redis队列的分布式负载均衡
 */
@Service
@Slf4j
public class QueueBalancer {
    
    @Autowired
    private RedisChgService redisChgService;

    /**
     * 通用的负载均衡获取队列方法
     * @param enumClass 枚举类的Class对象
     * @param redisKey  Redis队列的key
     * @return 负载均衡选中的队列枚举实例
     */
    public <T extends Enum<T>> T getQueueByPop(Class<T> enumClass, String redisKey) {
        // 初始化队列（如果需要）
        initializeQueue(enumClass, redisKey);
        
        // 从Redis队列中获取队列名称（轮询）
        String queueName = redisChgService.rpoplpush(redisKey);
        
        // 根据队列名称获取对应的枚举实例
        return fromName(queueName, enumClass);
    }
    
    /**
     * 根据名称获取枚举实例
     */
    public <T extends Enum<T>> T fromName(String name, Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null || enumConstants.length == 0) {
            throw new RuntimeException("获取枚举异常");
        }
        
        // 遍历枚举实例，匹配枚举名称
        for (T enumConstant : enumConstants) {
            if (name.equals(enumConstant.name())) {
                return enumConstant;
            }
        }
        
        // 未找到匹配项，返回第一个作为默认值
        log.warn("未找到匹配的队列名称: {}, 使用默认队列: {}", name, enumConstants[0].name());
        return enumConstants[0];
    }
    
    /**
     * 初始化队列
     */
    private <T extends Enum<T>> void initializeQueue(Class<T> enumClass, String redisKey) {
        Long queueLength = redisChgService.llen(redisKey);
        if (queueLength == 0) {
            String[] queueNames = getAllQueueNames(enumClass);
            redisChgService.rpush(redisKey, queueNames);
            log.warn("初始化redis队列 [{}]: {}", redisKey, Arrays.toString(queueNames));
        }
    }
    
    /**
     * 获取所有队列名称
     */
    public <T extends Enum<T>> String[] getAllQueueNames(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        if (enumConstants == null || enumConstants.length == 0) {
            throw new RuntimeException("获取枚举异常");
        }
        
        String[] queueNames = new String[enumConstants.length];
        for (int i = 0; i < enumConstants.length; i++) {
            // 直接使用枚举的名称，如 Q1, Q2, RECEIVE_1 等
            queueNames[i] = enumConstants[i].name();
        }
        
        return queueNames;
    }
}