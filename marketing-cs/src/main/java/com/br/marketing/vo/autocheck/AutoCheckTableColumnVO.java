package com.br.marketing.vo.autocheck;

import lombok.Data;

/**
 * information_schema.columns 查询结果承载对象。
 */
@Data
public class AutoCheckTableColumnVO {

    private String tableName;

    /**
     * 数据库列名（column_name）。
     */
    private String fieldName;

    /**
     * 列注释（column_comment）。
     */
    private String fieldDesc;
}


