package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_upload_data_field_dict
 * @author : zhen.Li1
 */
@Data
public class UploadDataFieldDict implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 客户编号
     */
    private String cid;

    /**
     * 扩展字段中key的集合
     */
    private String reserveField1Key;

    /**
     * 是否有效 1-有效；9-失效
     */
    private Integer isDel;

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