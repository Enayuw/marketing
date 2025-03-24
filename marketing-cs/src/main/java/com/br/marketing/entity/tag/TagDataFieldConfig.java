package com.br.marketing.entity.tag;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * t_tag_data_field_config
 * @author 
 */
@Data
public class TagDataFieldConfig implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 数据源编码
     */
    private String sourceCode;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段编码（实际数据库字段名）
     */
    private String fieldCode;

    /**
     * 字段类型（string/number/date等）
     */
    private String fieldType;

    /**
     * 字段值操作（input/select/datePicker等)
     */
    private String fieldOption;

    /**
     * 描述
     */
    private String description;

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

    private static final long serialVersionUID = 1L;
}