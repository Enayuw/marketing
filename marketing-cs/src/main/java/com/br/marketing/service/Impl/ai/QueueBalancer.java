package com.br.marketing.service.Impl.ai;

import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;

/**
 * 队列负载均衡器
 * 基于Redis队列的分布式负载均衡
 */
@Service
@Slf4j
public class QueueBalancer {

    public static final String LOCK = ":lock";
    @Autowired
    private RedisChgService redisChgService;

    /**
     * 通用的负载均衡获取队列方法
     * @param enumClass 枚举类的Class对象
     * @param redisKey  Redis队列的key
     * @return 负载均衡选中的队列枚举实例
     */
    public <T extends Enum<T>> T getQueueByPop(Class<T> enumClass, String redisKey) {
        try {
            // 初始化队列（如果需要）
            initializeQueue(enumClass, redisKey);

            // 从Redis队列中获取队列名称（轮询）
            String queueName = redisChgService.rpoplpush(redisKey);

            // 根据队列名称获取对应的枚举实例
            return fromName(queueName, enumClass);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), e.getMessage(),
                    "获取负载均衡队列异常，key：" + redisKey), e);
            T[] enumConstants = enumClass.getEnumConstants();
            return enumConstants[0];
        }
    }

    /**
     * 根据名称获取枚举实例
     */
    public <T extends Enum<T>> T fromName(String name, Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();

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
            String lockValue = UUID.randomUUID().toString();
            String lockKey = redisKey + LOCK;
            try {
                if (!redisChgService.lock(lockKey, lockValue, 30000L)) {
                    log.warn("负载均衡队列获取锁失败，key: {}", lockKey);
                    return;
                }

                if (redisChgService.llen(redisKey) > 0) {
                    return;
                }

                String[] queueNames = getAllQueueNames(enumClass);
                redisChgService.rpush(redisKey, queueNames);
                log.warn("初始化redis队列成功 [{}]: {}", redisKey, Arrays.toString(queueNames));
            } catch (Exception e) {
                log.error(AlertLog.buildWarnMessage(
                        AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                        e.getMessage(),
                        "获取负载均衡队列异常，key：" + redisKey), e);
            } finally {
                try {
                    if (!redisChgService.unlock(lockKey, lockValue)) {
                        log.error(AlertLog.buildWarnMessage(
                                AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                                "负载均衡队列释放锁失败，key：" + lockKey));
                    }
                } catch (Exception unlockEx) {
                    log.error(AlertLog.buildWarnMessage(
                            AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(),
                            "负载均衡队列释放锁时发生异常，key：" + lockKey));
                }
            }
        }
    }

    /**
     * 获取所有队列名称
     */
    public <T extends Enum<T>> String[] getAllQueueNames(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        String[] queueNames = new String[enumConstants.length];
        for (int i = 0; i < enumConstants.length; i++) {
            queueNames[i] = enumConstants[i].name();
        }

        return queueNames;
    }
}