package com.br.marketing.aspect;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.MqIdempotentContext;
import com.br.marketing.enums.MqIdempotentTableType;
import com.br.marketing.origin.MqFact;
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
        // 解析消息获取idempotentKey
        Long idempotentKey = extractIdempotentKey(joinPoint.getArgs());
        if (idempotentKey == null) {
            log.warn("消息中未找到idempotentKey，跳过幂等性检查");
            return joinPoint.proceed();
        }
        
        // 获取上下文信息
        String tag = MqIdempotentContext.getTag();
        String apiCode = MqIdempotentContext.getApiCode();
        MqIdempotentTableType tableType = mqIdempotent.tableType();
        
        // 尝试插入幂等记录
        Long recordId = insertIdempotentRecord(tableType, idempotentKey, apiCode, tag);
        if (recordId == null) {
            // DuplicateKeyException，消息已处理过，直接返回成功
            return createSuccessResult();
        }
        
        // 执行业务逻辑
        try {
            Object result = joinPoint.proceed();
            // 业务处理成功，更新apiCode
            updateApiCodeIfNeeded(tableType, recordId, apiCode);
            return result;
        } catch (Throwable e) {
            // 业务处理异常，删除幂等记录，让MQ重试
            deleteIdempotentRecordOnException(tableType, idempotentKey, recordId);
            throw e;
        } finally {
            // 清理ThreadLocal
            MqIdempotentContext.clear();
        }
    }
    
    /**
     * 从方法参数中提取idempotentKey
     */
    private Long extractIdempotentKey(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        
        Object firstArg = args[0];
        if (!(firstArg instanceof String)) {
            return null;
        }
        
        String message = (String) firstArg;
        try {
            MqFact mqFact = JSON.parseObject(message, MqFact.class);
            return mqFact != null ? mqFact.getIdempotentKey() : null;
        } catch (Exception e) {
            log.warn("解析MQ消息获取idempotentKey失败: {}", message, e);
            return null;
        }
    }
    
    /**
     * 插入幂等记录
     * @return 记录ID，如果返回null表示消息已处理过（DuplicateKeyException）
     */
    private Long insertIdempotentRecord(MqIdempotentTableType tableType, Long idempotentKey, 
                                        String apiCode, String tag) {
        try {
            Long recordId = mqIdempotentService.insertIdempotentRecord(tableType, idempotentKey, apiCode, tag);
            log.warn("幂等性检查通过，插入幂等记录成功，tableType: {}, idempotentKey: {}, recordId: {}, tag: {}", 
                    tableType.getCode(), idempotentKey, recordId, tag);
            return recordId;
        } catch (DuplicateKeyException e) {
            log.warn("消息已处理过（幂等性检查），tableType: {}, idempotentKey: {}, tag: {}, 跳过本次处理", 
                    tableType.getCode(), idempotentKey, tag);
            return null;
        } catch (Exception e) {
            log.error("插入幂等记录失败，tableType: {}, idempotentKey: {}, tag: {}", 
                    tableType.getCode(), idempotentKey, tag, e);
            // 插入失败不影响业务处理，返回一个特殊值表示需要继续执行
            return -1L;
        }
    }
    
    /**
     * 更新apiCode（如果需要）
     */
    private void updateApiCodeIfNeeded(MqIdempotentTableType tableType, Long recordId, String originalApiCode) {
        if (recordId == null || recordId < 0) {
            return;
        }
        
        String currentApiCode = MqIdempotentContext.getApiCode();
        if (currentApiCode == null || currentApiCode.equals(originalApiCode)) {
            return;
        }
        
        try {
            mqIdempotentService.updateApiCode(tableType, recordId, currentApiCode);
            log.warn("业务处理成功，更新幂等记录apiCode，tableType: {}, recordId: {}, apiCode: {}", 
                    tableType.getCode(), recordId, currentApiCode);
        } catch (Exception e) {
            log.warn("更新幂等记录apiCode失败，tableType: {}, recordId: {}", 
                    tableType.getCode(), recordId, e);
        }
    }
    
    /**
     * 业务异常时删除幂等记录
     */
    private void deleteIdempotentRecordOnException(MqIdempotentTableType tableType, 
                                                   Long idempotentKey, Long recordId) {
        if (recordId == null || recordId < 0) {
            return;
        }
        
        try {
            mqIdempotentService.deleteIdempotentRecord(tableType, recordId);
            log.warn("业务处理异常，已删除幂等记录，tableType: {}, idempotentKey: {}, recordId: {}, 等待MQ重试", 
                    tableType.getCode(), idempotentKey, recordId);
        } catch (Exception e) {
            log.error("删除幂等记录失败，tableType: {}, idempotentKey: {}, recordId: {}", 
                    tableType.getCode(), idempotentKey, recordId, e);
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

