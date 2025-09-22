package com.br.marketing.service.ftp;

import com.br.marketing.entity.SftpUploadTask;

public interface SftpUploadHandlerService {

    void insertSftpUploadTask(String apiCode, String localPath, String fileName,
                              Integer dataType, String postSqlProcess);


}
