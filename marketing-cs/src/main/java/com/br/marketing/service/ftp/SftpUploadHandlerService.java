package com.br.marketing.service.ftp;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.SftpUploadTargetParamDTO;

public interface SftpUploadHandlerService {

    Result insertSftpUploadTask(String apiCode, String localPath, String fileName,
                                Integer dataType, String postSqlProcess);

    /**
     * 插入SFTP上传任务记录（含推送目标配置，pushTargetType=1 时不查配置）
     *
     * @param param 入参（apiCode、localPath、fileName、dataType、postSqlProcess、pushTargetType、目标 SFTP 等）
     * @return 插入结果
     */
    Result insertSftpUploadTarget(SftpUploadTargetParamDTO param);

}
