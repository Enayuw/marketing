package com.br.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SFTP 上传任务（含推送目标）入参，用于 insertSftpUploadTarget
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SftpUploadTargetParamDTO {
    /** 商户编号 */
    private String apiCode;
    /** 本地文件路径 */
    private String localPath;
    /** 文件名称 */
    private String fileName;
    /** 文件类型 */
    private Integer dataType;
    /** 后置SQL处理 */
    private String postSqlProcess;
    /** 推送目标类型：0-从配置中获取，1-指定目标路径 */
    private Integer pushTargetType;
    /** 目的 SFTP host */
    private String targetSftpHost;
    /** 目的 SFTP port */
    private Integer targetSftpPort;
    /** 目的 SFTP 账号 */
    private String targetSftpUser;
    /** 目的 SFTP 密码 */
    private String targetSftpPwd;
    /** 公司文件服务器类型，见 FileServerType */
    private String targetType;
    /** 指定目标路径（pushTargetType=1 时必填） */
    private String targetPath;
}
