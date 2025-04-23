package com.br.marketing.entity.tag;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * t_tag_data_source_config
 * @author 
 */
@Data
public class TagDataSourceConfig implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 数据表名称 分表transfer_${apiCode}
     */
    private String sourceName;

    /**
     * 数据源编码CALL/TRANSFER/
     */
    private String sourceCode;

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