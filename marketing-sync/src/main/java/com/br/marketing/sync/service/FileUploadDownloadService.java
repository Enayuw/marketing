package com.br.marketing.sync.service;

import com.br.marketing.entity.FileSyncTask;
import com.br.marketing.entity.SyncConfig;

import java.util.List;

public interface FileUploadDownloadService {
    void processUploadTask(FileSyncTask uploadTask);

    Boolean updateTaskStatus(Long taskId, Integer status);

    void processDownloadTask(List<SyncConfig> loanSyncConfigs);

    void processFileSync(int type);

    void processUploadMiNioTask(FileSyncTask uploadTask);


}
