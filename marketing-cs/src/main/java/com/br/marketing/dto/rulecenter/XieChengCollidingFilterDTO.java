package com.br.marketing.dto.rulecenter;

import lombok.Data;

import java.util.Map;
/**
 * 规则中心-携程撞库筛选字段
 *
 * @author zhen.Li1
 * @dateTime 2024/04/25 14:14
 */
@Data
public class XieChengCollidingFilterDTO {

    /**
     * 撞库result
     */
    private String result;

    /**
     * 清洗日期
     */
    private String cleanTime;

    /**
     * release_time
     */
    private Map<String,Object> releaseTime;

    /**
     * coupon_code
     * 卷码code：result=true时返回
     */
    private Map<String,Object> coupon_code;

    /**
     * coupon_desc
     * 卷码描述：result=true时返回
     */
    private Map<String,Object> coupon_desc;

    /**
     * 客群名称
     */
    private String customerGroup;

    /**
     * 动态补充包剔除标识
     */
    private String info;

    /**
     * 黑名单剔除标识
     */
    private String blacklist_delete;

}
