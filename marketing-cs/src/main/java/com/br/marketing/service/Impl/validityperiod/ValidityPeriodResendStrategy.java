package com.br.marketing.service.Impl.validityperiod;

import com.alibaba.fastjson.JSONObject;
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
     * 构建重推数据扩展字段
     *
     * @param params params
     * @return {@link JSONObject }
     * @author senyang.zheng
     * @date 2023/11/08
     */
    JSONObject buildResendData(Map<String, Object> params);

    /**
     * 获取重推数据
     *
     * @param record   有效期重新发送记录
     * @param page     页码
     * @param pageSize 页大小
     * @return {@link List }<{@link T }>
     * @author senyang.zheng
     * @date 2023/11/20
     */
    List<T> fetchData(ValidityPeriodResendRecord record,int page,int pageSize);

    /**
     * 处理重推逻辑
     *
     * @param data   重推数据
     * @param record 有效期重新发送记录
     * @author senyang.zheng
     * @date 2023/11/09
     */
    void resend(List<T> data,ValidityPeriodResendRecord record);
}
