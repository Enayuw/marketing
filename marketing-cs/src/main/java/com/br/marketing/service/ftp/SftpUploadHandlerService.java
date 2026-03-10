package com.br.marketing.service.ftp;

import com.br.marketing.common.commondto.Result;

public interface SftpUploadHandlerService {

    Result insertSftpUploadTask(String apiCode, String localPath, String fileName,
                                Integer dataType, String postSqlProcess);

    /**
     * 插入SFTP上传任务记录（含推送目标配置，pushTargetType=1 时不查配置）
     *
     * @param apiCode        商户编号
     * @param localPath      本地文件路径
     * @param fileName       文件名称
     * @param dataType       文件类型
     * @param postSqlProcess 后置SQL处理
     * @param pushTargetType 推送目标类型：0-从配置中获取，1-指定目标路径
     * @param targetSftpHost 目的sftp host
     * @param targetSftpPort 目的sftp port
     * @param targetSftpUser 目的sftp 账号
     * @param targetSftpPwd  目的sftp 账号密码
     * @param targetType     公司文件服务器类型 见类：FileServerType
     * @param targetPath     指定目标路径（pushTargetType=1 时必填）
     * @return 插入结果
     */
    Result insertSftpUploadTaskWithTarget(String apiCode, String localPath, String fileName,
                                         Integer dataType, String postSqlProcess,
                                         Integer pushTargetType, String targetSftpHost, Integer targetSftpPort,
                                         String targetSftpUser, String targetSftpPwd, String targetType, String targetPath);

}
