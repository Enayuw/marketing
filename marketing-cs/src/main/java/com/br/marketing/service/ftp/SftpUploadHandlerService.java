package com.br.marketing.service.ftp;

public interface SftpUploadHandlerService {

    void insertSftpUploadTask(String apiCode, String localPath, String fileName,
                              Integer dataType, String postSqlProcess);

}
