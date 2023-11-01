package com.br.marketing.service;

/**
 * 有效期重新发送记录相关Service
 *
 * @author senyang.zheng
 * @date 2023/10/18
 */
public interface ValidityPeriodResendRecordService {

    /**
     * 保存重推记录
     *
     * @param apiCode          apiCode
     * @param userType         场景
     * @param validityPeriodId 有效期配置主键id
     * @author senyang.zheng
     * @date 2023/10/18
     */
    void saveRecord(String apiCode, String userType, Long validityPeriodId);
}
