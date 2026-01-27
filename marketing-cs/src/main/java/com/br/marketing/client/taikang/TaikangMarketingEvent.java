package com.br.marketing.client.taikang;

import lombok.Data;

/**
 * -----------------------------
 * PackageName： com.br.marketing.client.taikang
 * ClassName：TaikangMarketingEvent
 * Description：
 *
 * @author：it-yml CreateTime：2025-11-21
 * -----------------------------
 */
@Data
public class TaikangMarketingEvent {
    /**
     * 营销事件类型
     * 是否必传: 是
     * 示例: "channel_browse_interruption"
     * 说明: 默认值
     */
    private String eventId;

    /**
     * 渠道编码
     * 是否必传: 是
     * 说明: 由泰康在线分配
     */
    private String channelCode;

    /**
     * 客户ID
     * 是否必传: 否
     * 示例: "123456789"
     * 说明: 字符串
     */
    private String cusId;

    /**
     * 投保人姓名
     * 是否必传: 是/否
     * 示例: "张三"
     * 说明: 字符串
     */
    private String applicantName;

    /**
     * 投保人手机号
     * 是否必传: 是/否
     * 示例: "13111112222"
     * 说明: 字符串
     */
    private String applicantPhone;

    /**
     * 投保人证件号
     * 是否必传: 否
     * 示例: "340823199408164429"
     * 说明: 字符串
     */
    private String applicantIdNum;

    /**
     * fromid
     * 是否必传: 否
     * 示例: "72841"
     * 说明: 字符串
     */
    private String fromId;

    /**
     * 产品名称
     * 是否必传: 否
     * 示例: "百万医疗"
     * 说明: 字符串
     */
    private String productName;

    /**
     * 病种（选择项）
     * 是否必传: 否
     * 示例: "乳腺结节"
     * 说明: 字符串
     */
    private String sickName;

    /**
     * 投保链接
     * 是否必传: 否
     * 说明: 字符串
     */
    private String url;

    /**
     * 浏览时间
     * 是否必传: 是/否
     * 示例: "2024-12-12 15:04:00"
     * 说明: 字符串
     */
    private String browseDate;

    /**
     * 销售方案号
     * 是否必传: 否
     * 说明: 字符串，根据映射关系转换
     */
    private String productId;

    /**
     * 病种（投保链接）
     * 是否必传: 否
     */
    private String diseaseType;

    /**
     * 投保人性别
     * 是否必传: 否
     * 说明: 1：男，2：女，3：未知
     */
    private String applicantSex;

    /**
     * 投保人年龄
     * 是否必传: 否
     * 说明: 字符串
     */
    private String applicantAge;

    /**
     * 线索id
     * 是否必传: 否
     * 说明: 字符串
     */
    private String clueId;

    /**
     * 微信unionId
     * 是否必传: 否
     * 说明: 字符串
     */
    private String unionId;

    /**
     * ⽹电坐席id
     * 是否必传: 否
     * 说明: 字符串
     */
    private String seatId;


    /**
     * 流水号
     * 是否必传: 否
     * 说明: 字符串
     */
    private String flowNo;

    /**
     * 是否加微成功
     * 是否必传: 否
     * 说明: 字符串
     */
     private String addWeChat;

    /**
     * 备注
     * 是否必传: 否
     * 说明: 字符串
     */
    private String remark;
}
