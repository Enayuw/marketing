package com.br.marketing.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.MqIdempotentContext;
import com.br.marketing.enums.MqIdempotentTableType;
import com.br.marketing.service.MqIdempotentService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * MQ消息幂等性切面
 * 
 * 处理流程：
 * 1. 前置：尝试插入幂等记录，如果DuplicateKeyException则直接返回成功，跳过业务处理
 * 2. 后置：业务处理成功，更新apiCode（如果之前为null）
 * 3. 异常：业务处理异常，删除幂等记录，让MQ重试
 */
@Aspect
@Component
@Slf4j
public class MqIdempotentAspect {

    @Resource
    private MqIdempotentService mqIdempotentService;

    @Around("@annotation(mqIdempotent)")
    public Object around(ProceedingJoinPoint joinPoint, MqIdempotent mqIdempotent) throws Throwable {
        // 获取上下文信息
        String tag = MqIdempotentContext.getTag();
        String apiCode = MqIdempotentContext.getApiCode();
        MqIdempotentTableType tableType = mqIdempotent.tableType();

        try {
            // 提取幂等键
            Long idempotentKey = extractIdempotentKey(joinPoint.getArgs(), mqIdempotent, tag);
            if (idempotentKey == null) {
                log.warn("MQ消息中未找到idempotentKey，跳过幂等性检查(在服务上线过程中会出现，当生产者节点全部上线完成后不应再出现该消息！), tag: {}", tag);
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    // 业务处理异常，删除幂等记录，让MQ重试
                    throw new RuntimeException(e);
                }
            }

            // 尝试插入幂等记录（失败时抛出异常，让MQ重试）
            Long recordId = insertIdempotentRecord(tableType, idempotentKey, apiCode, tag);
            if (recordId == null) {
                // DuplicateKeyException，消息已处理过，直接返回成功
                return createSuccessResult();
            }

            // 执行业务逻辑
            try {
                Object result = joinPoint.proceed();
                // 业务处理成功，更新apiCode
                updateApiCodeIfNeeded(tableType, recordId, apiCode, tag);
                return result;
            } catch (Throwable e) {
                // 业务处理异常，删除幂等记录，让MQ重试
                deleteIdempotentRecordOnException(tableType, idempotentKey, tag);
                throw e;
            }
        } finally {
            MqIdempotentContext.clear();
        }
    }

    /**
     * 从方法参数中提取idempotentKey
     * 支持从 MqFact 对象或任意 JSON 中提取
     * @param args         方法参数
     * @param mqIdempotent 注解配置
     * @param tag          tag信息
     * @return idempotentKey，如果未找到返回null
     */
    private Long extractIdempotentKey(Object[] args, MqIdempotent mqIdempotent, String tag) {
        if (args == null || args.length == 0) {
            return null;
        }

        Object firstArg = args[0];
        if (!(firstArg instanceof String message)) {
            return null;
        }

        String idempotentKeyField = mqIdempotent.idempotentKeyField();

        try {
            // 先尝试解析为 JSONObject，直接获取字段
            JSONObject jsonObject = JSON.parseObject(message);
            if (jsonObject != null && jsonObject.containsKey(idempotentKeyField)) {
                Object value = jsonObject.get(idempotentKeyField);
                if (value != null) {
                    if (value instanceof Long) {
                        return (Long) value;
                    } else if (value instanceof Number) {
                        return ((Number) value).longValue();
                    } else if (value instanceof String) {
                        try {
                            return Long.parseLong((String) value);
                        } catch (NumberFormatException e) {
                            log.warn("MQ幂等切面, idempotentKey字段值不是有效的Long类型: {}, tag: {}", value, tag);
                            return null;
                        }
                    }
                }
            }

            return null;
        } catch (Exception e) {
            log.warn("MQ幂等切面, 解析MQ消息获取idempotentKey失败，字段名: {}, tag: {}, message: {}", idempotentKeyField, tag, message, e);
            return null;
        }
    }

    /**
     * 插入幂等记录
     * @return 记录ID，如果返回null表示消息已处理过（DuplicateKeyException）
     * @throws RuntimeException 插入失败时抛出异常，让MQ重试
     */
    private Long insertIdempotentRecord(MqIdempotentTableType tableType, Long idempotentKey,
                                        String apiCode, String tag) throws RuntimeException {
        try {
            return mqIdempotentService.insertIdempotentRecord(tableType, idempotentKey, apiCode, tag);
        } catch (DuplicateKeyException e) {
            String subject = "MQ幂等切面, 幂等校验不通过！";
            String message = String.format("该MQ消息已处理过, idempotentKey: %s, tag: %s, 跳过本次处理, error: %s",
                    idempotentKey, tag, e.getMessage());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), message
                    , subject), e);
            return null;
        } catch (Exception e) {
            deleteIdempotentRecordOnException(tableType, idempotentKey, tag);
            // 插入失败，无法保证幂等性，抛出异常让MQ重试（最多16次）
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * 更新apiCode
     */
    private void updateApiCodeIfNeeded(MqIdempotentTableType tableType, Long recordId, String originalApiCode, String tag) {
        String currentApiCode = MqIdempotentContext.getApiCode();
        if (currentApiCode == null || currentApiCode.equals(originalApiCode)) {
            return;
        }

        try {
            mqIdempotentService.updateApiCode(tableType, recordId, currentApiCode);
        } catch (Exception e) {
            String subject = "MQ幂等切面, 更新幂等记录apiCode失败";
            String errorMsg = String.format("更新幂等记录apiCode失败, recordId: %s, tag: %s, apiCode: %s, error: %s",
                    recordId, tag, currentApiCode, e.getMessage());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), errorMsg
                    , subject), e);
        }
    }

    /**
     * 业务异常时删除幂等记录（带重试机制）
     * 使用幂等键删除，删除失败时进行有限次数的重试，提高删除成功率
     */
    private void deleteIdempotentRecordOnException(MqIdempotentTableType tableType,
                                                   Long idempotentKey, String tag) {
        if (idempotentKey == null) {
            return;
        }

        String apiCode = MqIdempotentContext.getApiCode();
        int maxRetries = 3;
        int retryCount = 0;
        boolean deleted = false;

        while (retryCount < maxRetries && !deleted) {
            try {
                mqIdempotentService.deleteIdempotentRecordByKey(tableType, idempotentKey);
                log.warn("MQ幂等切面，业务处理异常，已通过幂等键删除幂等记录, idempotentKey: {}, 重试次数: {}, tag: {}, apiCode: {}, 等待MQ重试",
                        idempotentKey, retryCount, tag, apiCode);
                deleted = true;
            } catch (Exception e) {
                retryCount++;
                if (retryCount < maxRetries) {
                    log.warn("MQ幂等切面，通过幂等键删除幂等记录失败，准备重试，idempotentKey: {}, 重试次数: {}/{}, tag: {}, apiCode: {}",
                            idempotentKey, retryCount, maxRetries, tag, apiCode, e);
                    try {
                        // 递增延迟：100ms, 200ms, 300ms
                        Thread.sleep(100L * retryCount);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("MQ幂等切面，删除幂等记录重试延迟被中断，idempotentKey: {}, tag: {}, apiCode: {}",
                                idempotentKey, tag, apiCode, ie);
                        break;
                    }
                } else {
                    // 重试失败，记录告警
                    String errorMsg = String.format("MQ幂等切面，删除幂等记录失败（已重试%d次），" +
                                    "幂等记录可能残留 idempotentKey: %s, tag: %s, apiCode: %s, error: %s",
                            maxRetries, idempotentKey, tag, apiCode != null ? apiCode : "null", e.getMessage());
                    String subject = "MQ幂等切面，删除幂等记录失败";
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), errorMsg
                            , subject), e);
                }
            }
        }
    }

    /**
     * 创建成功结果
     */
    private Object createSuccessResult() {
        Result<Boolean> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getValue());
        result.setDate(false);
        return result;
    }
}

