package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

/**
 * 有效期重新发送记录表
 *
 * @author senyang.zheng
 * @date 2023/10/10
 */
@Data
public class ValidityPeriodResendRecord {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 客户apiCode
     */
    private String apiCode;

    /**
     * 有效期配置主键id
     */
    private Long validityPeriodId;

    /**
     * 重推类型
     */
    private Integer resendType;

    /**
     * 重推扩展字段
     */
    private String resendData;

    /**
     * 状态 0:待执行;1:执行完成;
     */
    private Integer resendStatus;

    /**
     * 是否删除 0:否;1:是;
     */
    private Integer isDelete;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

}