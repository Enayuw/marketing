package com.br.marketing.file.service.ftp;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.entity.SyncConfig;

import java.util.List;
import java.util.Map;

/**
 * 
 */
public interface FtpService {

    BaseFtpClient getClient(SyncConfig syncConfig, boolean isSrc);
    Map<String, List<String>> listFileBySuffixFromSource(SyncConfig syncConfig);
    void copyFile(SyncConfig syncConfig, String fileName, BaseFtpClient srcClient, BaseFtpClient targetClient);
}
