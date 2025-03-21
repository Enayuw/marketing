package com.br.marketing.dto.tag;

import lombok.Data;

@Data
public class MaterializedViewDTO {

    /**
     */
    private Integer Id;

    /**
     * 物化视图名称
     */
    private String Name;

    /**
     * 物化视图构建 Job 的名称
     */
    private String JobName;

    /**
     * 状态
     */
    private String State;

    /**
     * 表示 SCHEMA_CHANGE 发生的原因
     */
    private String SchemaChangeDetail;

    /**
     * 最后一次任务刷新的状态
     */
    private String RefreshState;

    /**
     *
     */
    private String RefreshInfo;


    /**
     */
    private String QuerySql;


    /**
     */
    private String EnvInfo;
    /**
     *
     */
    private String MvProperties;
    /**
     *
     */
    private String MvPartitionInfo;
    /**
     *是否和基表数据同步。1 为同步，0 为不同步
     */
    private String SyncWithBaseTables;





}
