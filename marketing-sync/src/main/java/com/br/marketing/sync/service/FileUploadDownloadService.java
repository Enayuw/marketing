package com.br.marketing.sync.service;

import com.br.marketing.entity.SftpUploadTask;

public interface FileUploadDownloadService {
    void processUploadTask(SftpUploadTask uploadTask);

    boolean updateTaskStatus(Long taskId, Integer status);

}
