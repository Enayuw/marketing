package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

@Data
public class SynInfoQueryAction {
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 数据Id
     */
    private Long dataId;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 商户编号
     */
    private String apiCode;

    /**
     * action状态 1-处理中,2-成功,3-失败
     */
    private Integer actionStatus;

    /**
     * 查询日期
     */
    private String actionDate;

    /**
     * 0-正常,1-逻辑删除
     */
    private Integer deleteFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}