package com.br.marketing.aspect;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.context.MqIdempotentContext;
import com.br.marketing.entity.IdempotentRecordInfo;
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
 * 1. 前置：尝试插入幂等记录
 *    - 插入成功：继续执行业务逻辑
 *    - DuplicateKeyException：根据幂等键查询已存在记录
 *      - 如果isFinished=1：说明业务已执行完成，返回成功，跳过业务处理
 *      - 如果isFinished=0：说明业务未执行完成（节点下线或业务失败），返回记录ID，继续执行业务
 * 2. 后置：业务处理成功，更新isFinished=1（无论apiCode是否存在）
 * 3. 异常：业务处理异常，不删除幂等记录（isFinished=0），让MQ重试
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
                    throw new RuntimeException("业务处理异常，没有幂等键，让MQ重试，apiCode：" + apiCode + "，tag:" +
                            tag + "args:" + JSON.toJSONString(joinPoint.getArgs()), e);
                }
            }

            // 尝试插入幂等记录（失败时抛出异常，让MQ重试）
            Long recordId = insertIdempotentRecord(tableType, idempotentKey, apiCode, tag);
            if (recordId == null) {
                return createSuccessResult();
            }

            // 执行业务逻辑
            try {
                Object result = joinPoint.proceed();
                // 业务处理成功，更新isFinished=1（无论apiCode是否存在）
                updateIsFinishedAndApiCode(tableType, recordId, tag);
                return result;
            } catch (Throwable e) {
                // 业务处理异常，不删除幂等记录（isFinished=0），让MQ重试
                throw new RuntimeException("业务处理异常，让MQ重试，apiCode：" + apiCode + "，tag:" +
                        tag + "，idempotentKey：" + idempotentKey + "args:" + JSON.toJSONString(joinPoint.getArgs()), e);
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
     * 更新isFinished和apiCode
     */
    private void updateIsFinishedAndApiCode(MqIdempotentTableType tableType, Long recordId, String tag) {
        String currentApiCode = MqIdempotentContext.getApiCode();

        try {
            mqIdempotentService.updateIsFinishedAndApiCode(tableType, recordId, currentApiCode);
        } catch (Exception e) {
            String subject = "MQ幂等切面, 更新幂等记录isFinished失败";
            String errorMsg = String.format("更新幂等记录isFinished失败, recordId: %s, tag: %s, apiCode: %s, error: %s",
                    recordId, tag, currentApiCode, e.getMessage());
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), errorMsg
                    , subject), e);
        }
    }

    /**
     * 插入幂等记录
     * @return 记录ID，如果返回null表示消息已处理过（DuplicateKeyException且isFinished=1）
     * @throws RuntimeException 插入失败时抛出异常，让MQ重试
     */
    private Long insertIdempotentRecord(MqIdempotentTableType tableType, Long idempotentKey,
                                        String apiCode, String tag) throws RuntimeException {
        try {
            return mqIdempotentService.insertIdempotentRecord(tableType, idempotentKey, apiCode, tag);
        } catch (DuplicateKeyException e) {
            // 根据幂等键查询已存在的记录
            IdempotentRecordInfo existingRecord = mqIdempotentService.selectByIdempotentKey(tableType, idempotentKey);

            // 理论上不应该出现
            if (existingRecord == null) {
                String subject = "MQ幂等切面, 幂等校验异常！";
                String message = String.format("DuplicateKeyException但查询不到记录, idempotentKey: %s, tag: %s, error: %s",
                        idempotentKey, tag, e.getMessage());
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), message, subject), e);
                return null;
            }

            // 判断isFinished是否为1
            Integer isFinished = existingRecord.getIsFinished();
            if (isFinished != null && isFinished == 1) {
                // isFinished=1，说明业务已执行完成，跳过本次处理
                String subject = "MQ幂等切面, 幂等校验不通过！";
                String message = String.format("该MQ消息已处理过, idempotentKey: %s, tag: %s, isFinished: %d, 跳过本次处理, error: %s",
                        idempotentKey, tag, isFinished, e.getMessage());
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.YINGXIAO_SERVICEERROR.getCode(), message, subject), e);
                return null;
            } else {
                // isFinished=0或null，说明业务未执行完成（节点下线或业务失败），返回记录ID，让业务继续执行
                log.warn("MQ幂等切面, 检测到isFinished=0，认为业务未执行完成，重新执行业务, idempotentKey: {}, tag: {}, recordId: {}",
                        idempotentKey, tag, existingRecord.getId());
                return existingRecord.getId();
            }
        } catch (Exception e) {
            // 插入失败，无法保证幂等性，抛出异常让MQ重试（最多16次）
            throw new RuntimeException("insertIdempotentRecord插入失败，无法保证幂等性，抛出异常让MQ重试" + e.getMessage(), e);
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


