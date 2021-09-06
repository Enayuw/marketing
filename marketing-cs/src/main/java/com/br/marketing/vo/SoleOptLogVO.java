package com.br.marketing.vo;

import lombok.Data;

import java.util.Date;

@Data
public class SoleOptLogVO {

    /**
     * 去重规则id
     */
    private String soleId;

    /**
     * 去重规则名称
     */
    private String soleName;

    /**
     * 去重字段
     */
    private String soleFields;

    /**
     * 去重时间周期
     */
    private String soleCycleTimes;

    /**
     * 匹配商户
     */
    private String soleCustomers;

    /**
     * 使用状态 启用/禁用
     */
    private String status;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 操作人id
     */
    private String optUserId;

    /**
     * 操作人姓名
     */
    private String optUserName;

}
