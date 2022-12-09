package com.br.marketing.service;

import com.br.marketing.client.BaseFtpClient;
import com.br.marketing.client.SftpClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.SyncConfig;

import java.util.List;

public interface IFileActionService {

    /**
     * 从sftp上下载文件
     * @param client
     * @param sourcePath
     * @param targetPath
     * @param fileName
     * @return
     */
    Result downFileBySftp(SftpClient client, String sourcePath, String targetPath,String fileName);

    /**
     * 根据sftp配置下载文件，并且变更sftp上文件名称
     * @param client
     * @param syncConfig
     * @param targetPath
     * @return
     */
    Result<List<String>> downSyncFileBySftp(SftpClient client, SyncConfig syncConfig, String targetPath);
}
