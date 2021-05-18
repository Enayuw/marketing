package com.br.marketing.mapper;
import com.br.marketing.entity.SyncConfig;

import java.util.List;

public interface SyncConfigMapper extends SyncConfigMapperBase {
    /**
     * 插入文件同步配置
     * @param loanSyncConfig 配置对象
     */
    void insertConfig(SyncConfig loanSyncConfig);

    /**
     * 查询可用的配置
     * @return 可用配置列表
     */
    List<SyncConfig> queryConfig(String type);

    SyncConfig queryConfigByConditaion(SyncConfig loanSyncConfig);
}