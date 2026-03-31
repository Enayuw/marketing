package com.br.marketing.client.rulecleaning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 清洗配置DTO
 *
 * @author zhen.Li1
 * @date 2025/06/12
 */
@Data
@Schema(description = "清洗配置DTO传输对象")
public class CleanConfigDTO implements Serializable {

    @Schema(description = "API编码")
    @NotNull(message = "API编码不能为空")
    private String apiCode;

    @Schema(description = "接口用途：0上传，1转化")
    @NotNull(message = "接口用途不能为空")
    private Integer dataType;

    @Schema(description = "接口类型：0通用,1定制,2FTP")
    @NotNull(message = "接口类型不能为空")
    private Integer acceptType;

    @Schema(description = "文件类型：13:上传清洗周期文件,14:转化清洗周期文件")
    private Integer fileType;

    @Schema(description = "数据来源：0营销中台,1外呼系统")
    @NotNull(message = "数据来源不能为空")
    private Integer systemType;

    @Schema(description = "文件路径")
    private String sftpPath;

    @Schema(description = "SFTP 文件分隔符，不传或空时落库为逗号")
    private String sftpFileSeparator;

}
