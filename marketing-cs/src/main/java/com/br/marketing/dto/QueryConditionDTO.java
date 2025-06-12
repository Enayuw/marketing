package com.br.marketing.dto;

import lombok.Data;



/**
 * @ClassName ExecuteCarClueDTO
 * @Description 中台数据导出
 * @Author kongbx
 * @Date 2025/5/6 14:51
 */
@Data
public class QueryConditionDTO {
    private String tableName;
    private String whereClause;
    private String orderBy;
    // 分页大小
    private Integer pageSize = 1000;
}
