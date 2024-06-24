package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class XieChengCollidingDataLog {
    /**
     * 主键id
     */
    private Long id;

    /**
     *
     */
    private Long smsCollidingDataId;

    /**
     * 规则包记录id
     */
    private Long packageId;

    /**
     * 撞库规则id
     */
    private Long packageRuleId;

    /**
     * 数据来源类型：T -周期，F-代表非周期
     */
    private String dataSourceType;

    /**
     * 手机号
     */
    private String cellSha256CodeList;

    /**
     * 数据释放时间，下次撞库时间
     */
    private String releaseTime;

    /**
     * 释放日期
     */
    private String releaseDate;

    /**
     * 携程用户：CTRIP 去哪儿用户：QUNAR
     */
    private String orgChannel;

    /**
     * 营销档位（具体值由运营同学实际定义为准）如：重点营销，次重点营销
     */
    private String mktLevel;

    /**
     * 手机号当前因保护期等原因导致暂时不能营销，但后续可重新撞库判断是否可营销,返回值：后续可再次撞库
     */
    private String info;

    /**
     * 核验结果 true：参与营销，false：不参与营销
     */
    private Boolean result;

    /**
     * 券码信息
     */
    private String marketCouponList;

    /**
     * 券码code
     */
    private String couponCode;

    /**
     * 券码名称
     */
    private String couponDesc;

    /**
     * 网络异常码
     */
    private Integer httpCode;

    /**
     * 业务异常码
     */
    private Integer businessCode;

    /**
     * 接口返回内容
     */
    private String returnContent;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 状态 0-正常1 删除
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;
}