package com.br.marketing.service.Impl.validityperiod;

import java.util.List;

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
     * @param id id
     * @return {@link List }<{@link T }>
     * @author senyang.zheng
     * @date 2023/10/08
     */
    List<T> fetchData(Long id);

    /**
     * 处理重推逻辑
     *
     * @param data 重推数据
     * @author senyang.zheng
     * @date 2023/10/08
     */
    void resend(List<T> data);
}
