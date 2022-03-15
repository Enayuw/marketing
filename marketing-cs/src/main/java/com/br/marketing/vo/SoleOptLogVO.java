package com.br.marketing.vo;

import lombok.Data;

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
    private Integer soleCycleTimes;

    /**
     * 匹配商户
     */
    private String customerInfo;

    /**
     * 使用状态 启用/禁用
     */
    private Integer status;

    /**
     * 修改时间
     */
    private String updateTime;

    /**
     * 操作人id
     */
    private String optUserId;

    /**
     * 操作人姓名
     */
    private String optUserName;

}
