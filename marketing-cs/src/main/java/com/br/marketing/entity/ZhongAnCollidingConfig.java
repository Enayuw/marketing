package com.br.marketing.entity;

import java.util.Date;
import lombok.Data;

@Data
public class ZhongAnCollidingConfig {
    /**
     *
     */
    private Long id;

    /**
     * 优先级(1~100，值越小，优先级越高)
     */
    private Integer priority;

    /**
     * 数据来源类型：C&S-已接通&已发短信，C&NS=已接通&未发短信，NC&S-未接通&发短信，S-只发短信，NC-未接通
     */
    private String dataSourceType;

    /**
     * 查询逻辑说明
     */
    private String queryDesc;

    /**
     * 状态 0-正常 1-删除
     */
    private Integer isDeleted;

    /**
     * 扩展字段
     */
    private String extend;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    /**
     * 查询数据sql
     */
    private String querySql;
}