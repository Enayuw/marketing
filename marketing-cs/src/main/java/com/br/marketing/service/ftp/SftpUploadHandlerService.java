package com.br.marketing.service.ftp;

import com.br.marketing.common.commondto.Result;

public interface SftpUploadHandlerService {

    Result insertSftpUploadTask(String apiCode, String localPath, String fileName,
                                Integer dataType, String postSqlProcess);

}
