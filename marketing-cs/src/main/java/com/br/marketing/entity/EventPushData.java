package com.br.marketing.entity;

import lombok.Data;

/**
 * @ClassName EventList
 * @Author hang.zhou
 * @Date 2025/11/17
 */
@Data
public class EventPushData {

    /**
     * 人群下发唯一识别码
     */
    private String serialNo;

    /**
     * 模板编号（人群下发时的模板编号）
     */
    private String templateNo;

    /**
     * 事件类型：REALTIME_LIMIT_INCREASE-实时提额, REALTIME_PRICE_DROP-实时降价, REALTIME_LOAN-实时借款, REALTIME_LOGIN-实时登录
     */
    private String eventType;

    /**
     * 请求流水号
     */
    private String flowNo;

}
