package com.br.marketing.service.ruleCleaning.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 字段样例DTO
 * @author guangxiu.li
 * @date 2025/5/6
 */
@Data
public class FieldSampleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段样例值
     */
    private String fieldSample;

    /**
     * 初次上传时间
     */
    private Date firstUploadTime;

    /**
     * 关联字段
     */
    private String relatedField;

    /**
     * 是否需要清洗
     */
    private Boolean needCleaning;
} 