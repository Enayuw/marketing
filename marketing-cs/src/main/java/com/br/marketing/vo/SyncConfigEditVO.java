package com.br.marketing.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class SyncConfigEditVO {
    /**
     * 
     */
    @Schema(description = "主键id")
    private Long id;

    /**
     * 商户编号
     */
    @Schema(description = "apiCode")
    private String apiCode;

    /**
     * 文件类型  1 数据文件 2 错误文件
     */
    @Schema(description = "文件类型。1:跑分上传文件,2:错误文件,3:电销文件,4:七七撞库文件")
    private Integer dataType;

    @Schema(description = "文件类型。1:跑分上传文件,2:错误文件,3:电销文件,4:七七撞库文件")
    private String dataTypeValue;

    /**
     * 需要同步客户文件的源目录。
     */
    @Schema(description = "源目录")
    private String srcPath;

    /**
     * 需要同步到公司文件的目的目录
     */
    @Schema(description = "目的目录")
    private String targetPath;


    /**
     * 文件后缀，以逗号分隔。例如：".zip,.success"
     */
    @Schema(description = "文件后缀,以逗号分隔。例如：.zip,.success")
    private String suffix;

    /**
     * 源sftp host
     */
    @Schema(description = "源sftp host")
    private String srcSftpHost;

    /**
     * 源sftp port
     */
    @Schema(description = "源sftp端口")
    private Integer srcSftpPort;

    /**
     * 源sftp账号
     */
    @Schema(description = "源sftp账号")
    private String srcSftpUser;

    /**
     * 源sftp账号密码
     */
    @Schema(description = "源sftp账号密码")
    private String srcSftpPwd;

    /**
     * 目的sftp host
     */
    @Schema(description = "目的sftp host")
    private String targetSftpHost;

    /**
     * 目的sftp port
     */
    @Schema(description = "目的sftp端口")
    private Integer targetSftpPort;

    /**
     * 目的sftp 账号
     */
    @Schema(description = "目的sftp账号")
    private String targetSftpUser;

    /**
     * 目的sftp 账号密码
     */
    @Schema(description = "目的sftp账号密码")
    private String targetSftpPwd;

    /**
     * 客户文件服务器类型
     */
    @Schema(description = "客户文件服务器类型")
    private String srcType;

    /**
     * 公司文件服务器类型
     */
    @Schema(description = "公司文件服务器类型")
    private String targetType;

    /**
     * 同步文件的类型。1：sftp>>本地磁盘，2：本地磁盘>>sftp
     */
    @Schema(description = "同步文件的类型。1：sftp>>本地磁盘，2：本地磁盘>>sftp")
    private Integer type;

    /**
     * 定时执行时间
     * {"day":"0","time":"12:00"}   day枚举：0:T-1  1:T
     */
    @Schema(description = "定时执行时间")
    private String executeTime;

    @Schema(description = "是否需要解压：0-否 1-是", implementation = Integer.class, allowableValues = {"0", "1"})
    private Integer isUnzip;

    @Schema(description = "解压文件名编码，默认 GBK", implementation = String.class, allowableValues = {"GBK", "UTF-8"})
    @JsonProperty("unzipfilenameCharset")
    private String unzipFilenameCharset;

}