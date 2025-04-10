package com.br.marketing.entity.tag;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * t_tag_data_rule
 * @author 
 */
@Data
public class TagDataRule implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 标签编码
     */
    private String tagCode;

    /**
     * 标签名称
     */
    private String tagName;

    /**
     * 量级
     */
    private Integer tagNumber;

    /**
     * 标签规则内容
     */
    private String content;

    /**
     * 数据源编码,多个用,分割
     */
    private String sourceCode;

    /**
     * 标签内容总结
     */
    private String summary;

    /**
     * 时间量级
     */
    private Integer timeNumber;

    /**
     * 时间范围单位 d-天，m-月
     */
    private String timeUnit;

    /**
     * apiCode圈选范围展示冗余字段
     */
    private String apiCodeScope;

    /**
     * apiCode授权展示冗余字段
     */
    private String apiCodeLicense;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

    /**
     * 操作人id
     */
    private Long optUserId;

    /**
     * 操作人账户名
     */
    private String optUserName;

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