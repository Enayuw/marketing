package com.br.marketing.service.Impl.validityperiod;

import com.br.marketing.entity.ValidityPeriodResendRecord;

import java.util.List;
import java.util.Map;

/**
 * 有效期变更重推策略
 *
 * @author senyang.zheng
 * @date 2023/10/08
 */
public interface ValidityPeriodResendStrategy<T> {


    /**
     * 获取重推数据
     *
     * @param validityPeriodResendRecord 有效期重新发送记录
     * @return {@link List }<{@link T }>
     * @author senyang.zheng
     * @date 2023/10/11
     */
    List<T> fetchData(ValidityPeriodResendRecord validityPeriodResendRecord);

    /**
     * 处理重推逻辑
     *
     * @param data 重推数据
     * @author senyang.zheng
     * @date 2023/10/08
     */
    void resend(List<T> data);
}
