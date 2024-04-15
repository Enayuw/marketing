package com.br.marketing.service.Impl.validityperiod;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.aspect.ValidityPeriodResendType;
import com.br.marketing.entity.ValidityPeriodResendRecord;
import com.br.marketing.enums.ValidityPeriodResendEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 有效期变更重推策略选择器
 *
 * @author senyang.zheng
 * @date 2023/10/08
 */
@Slf4j
@Service
public class ValidityPeriodResendStrategySelector {

    private final Map<ValidityPeriodResendEnum, ValidityPeriodResendStrategy<?>> strategyMap;

    @Autowired
    public ValidityPeriodResendStrategySelector(List<ValidityPeriodResendStrategy<?>> strategies) {
        strategyMap = strategies.stream().collect(
            Collectors.toMap(s -> s.getClass().getAnnotation(ValidityPeriodResendType.class).resendType(), Function.identity()));
    }

    public <T> JSONObject buildResendData(Map<String, Object> params, ValidityPeriodResendEnum resendType) {
        // 根据数据类型选择对应的策略
        @SuppressWarnings("unchecked")
        ValidityPeriodResendStrategy<T> strategy = (ValidityPeriodResendStrategy<T>) this.strategyMap.get(resendType);
        if (strategy == null) {
            throw new IllegalArgumentException("未匹配到对应重推规则: " + resendType);
        }
        return strategy.buildResendData(params);
    }

    public <T> List<T> fetchData(ValidityPeriodResendRecord record, int page, int pageSize) {
        // 根据数据类型选择对应的策略
        @SuppressWarnings("unchecked")
        ValidityPeriodResendStrategy<T> strategy = (ValidityPeriodResendStrategy<T>) this.strategyMap.get(ValidityPeriodResendEnum.getEnumByCode(record.getResendType()));
        if (strategy == null) {
            throw new IllegalArgumentException("未匹配到对应重推规则: " + ValidityPeriodResendEnum.getEnumByCode(record.getResendType()));
        }
        return strategy.fetchData(record, page, pageSize);
    }

    public <T> void resend(List<T> data, ValidityPeriodResendRecord record) {
        // 根据枚举类型选择对应的策略
        @SuppressWarnings("unchecked")
        ValidityPeriodResendStrategy<T> strategy = (ValidityPeriodResendStrategy<T>) strategyMap.get(ValidityPeriodResendEnum.getEnumByCode(record.getResendType()));
        if (strategy == null) {
            throw new IllegalArgumentException("未匹配到对应重推规则: " + ValidityPeriodResendEnum.getEnumByCode(record.getResendType()));
        }
        strategy.resend(data, record);
    }
}
