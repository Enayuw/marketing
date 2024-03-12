package com.br.marketing.entity;

import lombok.Data;

import java.util.Date;

/**
 * 有效期变更重推规则映射实体
 *
 * @author senyang.zheng
 * @date 2023/11/13
 */
@Data
public class ValidityPeriodResendTypeMapping {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 客户apiCode
     */
    private String apiCode;

    /**
     * 场景
     */
    private String userType;

    /**
     * 重推类型
     */
    private Integer resendType;

    /**
     * 扩展字段
     */
    private String extendField;

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