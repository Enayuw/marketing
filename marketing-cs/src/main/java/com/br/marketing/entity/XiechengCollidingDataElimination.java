package com.br.marketing.entity;

import java.util.Date;

import lombok.Data;

@Data
public class XiechengCollidingDataElimination {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 业务表主键id
     */
    private Long bizId;

    /**
     * 数据类型：0：转化
     */
    private Integer bizType;

    /**
     * 手机号
     */
    private String cellSha256CodeList;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDelete;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

}