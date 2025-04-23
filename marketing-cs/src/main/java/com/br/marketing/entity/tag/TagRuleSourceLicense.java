package com.br.marketing.entity.tag;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * t_tag_rule_source_license
 * @author 
 */
@Data
public class TagRuleSourceLicense implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 标签编码
     */
    private String tagCode;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除 0：正常，1：删除
     */
    private Integer deleteFlag;

    private static final long serialVersionUID = 1L;
}