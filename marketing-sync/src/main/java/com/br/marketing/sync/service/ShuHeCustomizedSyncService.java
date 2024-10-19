package com.br.marketing.sync.service;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.entity.SyncConfig;

public interface ShuHeCustomizedSyncService {
    /**
     * 同步文件
     *
     * @param syncConfig   同步配置
     * @param srcClient
     * @param targetClient
     * @author senyang.zheng
     * @date 2024/10/18
     */
    void syncFile(SyncConfig syncConfig, BaseFtpClient srcClient, BaseFtpClient targetClient);
}
