package com.br.marketing.entity.tag;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * t_tag_data_source_mapping
 * @author 
 */
@Data
public class TagDataSourceMapping implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 标签可用数据源编码
     */
    private String sourceMappingCode;

    /**
     * apiCode
     */
    private String apiCode;

    /**
     * 明确的数据表名称
     */
    private String sourceName;

    /**
     * 表类型 1-base表；2-物化视图；
     */
    private Integer sourceType;

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