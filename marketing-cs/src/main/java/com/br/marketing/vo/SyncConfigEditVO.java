package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
public class SyncConfigEditVO {
    /**
     * 
     */
    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * 商户编号
     */
    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    /**
     * 文件类型  1 数据文件 2 错误文件
     */
    @ApiModelProperty(value = "文件类型。1:跑分上传文件,2:错误文件,3:电销文件,4:七七撞库文件")
    private Integer dataType;

    @ApiModelProperty(value = "文件类型。1:跑分上传文件,2:错误文件,3:电销文件,4:七七撞库文件")
    private String dataTypeValue;

    /**
     * 需要同步客户文件的源目录。
     */
    @ApiModelProperty(value = "源目录")
    private String srcPath;

    /**
     * 需要同步到公司文件的目的目录
     */
    @ApiModelProperty(value = "目的目录")
    private String targetPath;


    /**
     * 文件后缀，以逗号分隔。例如：".zip,.success"
     */
    @ApiModelProperty(value = "文件后缀,以逗号分隔。例如：.zip,.success")
    private String suffix;

    /**
     * 源sftp host
     */
    @ApiModelProperty(value = "源sftp host")
    private String srcSftpHost;

    /**
     * 源sftp port
     */
    @ApiModelProperty(value = "源sftp端口")
    private Integer srcSftpPort;

    /**
     * 源sftp账号
     */
    @ApiModelProperty(value = "源sftp账号")
    private String srcSftpUser;

    /**
     * 源sftp账号密码
     */
    @ApiModelProperty(value = "源sftp账号密码")
    private String srcSftpPwd;

    /**
     * 目的sftp host
     */
    @ApiModelProperty(value = "目的sftp host")
    private String targetSftpHost;

    /**
     * 目的sftp port
     */
    @ApiModelProperty(value = "目的sftp端口")
    private Integer targetSftpPort;

    /**
     * 目的sftp 账号
     */
    @ApiModelProperty(value = "目的sftp账号")
    private String targetSftpUser;

    /**
     * 目的sftp 账号密码
     */
    @ApiModelProperty(value = "目的sftp账号密码")
    private String targetSftpPwd;

    /**
     * 客户文件服务器类型
     */
    @ApiModelProperty(value = "客户文件服务器类型")
    private String srcType;

    /**
     * 公司文件服务器类型
     */
    @ApiModelProperty(value = "公司文件服务器类型")
    private String targetType;

    /**
     * 同步文件的类型。1：sftp>>本地磁盘，2：本地磁盘>>sftp
     */
    @ApiModelProperty(value = "同步文件的类型。1：sftp>>本地磁盘，2：本地磁盘>>sftp")
    private Integer type;

    /**
     * 定时执行时间
     * {"day":"0","time":"12:00"}   day枚举：0:T-1  1:T
     */
    @ApiModelProperty(value = "定时执行时间")
    private String executeTime;

}