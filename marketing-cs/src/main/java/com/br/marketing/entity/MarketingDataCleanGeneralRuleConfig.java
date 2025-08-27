package com.br.marketing.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_marketing_data_clean_general_rule_config
 * @author 
 */
@Data
public class MarketingDataCleanGeneralRuleConfig implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 清洗配置ID
     */
    private Long cleanConfigId;

    /**
     * API编码
     */
    private String apiCode;

    /**
     * 映射字段，映射字段唯一
     */
    private String mappingField;

    /**
     * 清洗字段，多个用,号分割
     */
    private String cleanFields;

    /**
     * 清洗字段层级
     */
    private Integer level;

    /**
     * 父节点完整路径
     */
    private String parentPath;

    /**
     * 是否映射：0-否，1-是
     */
    private Boolean isMapping;

    /**
     * 是否衍生：0:非衍生1:衍生
     */
    private Integer isDerived;

    /**
     * 映射规则
     */
    private String mappingRule;

    /**
     * 清洗结果预览
     */
    private String resultPreview;

    /**
     * 删除标志；1-正常；9-删除
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