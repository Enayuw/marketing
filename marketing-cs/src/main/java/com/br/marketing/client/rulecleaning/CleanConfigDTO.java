package com.br.marketing.client.rulecleaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 清洗配置DTO
 * @author zhen.Li1
 * @date 2025/06/12
 */
@Data
@ApiModel(value = "清洗配置DTO", description = "清洗配置DTO传输对象")
public class CleanConfigDTO implements Serializable {

    @ApiModelProperty(value = "API编码")
    @NotNull(message = "API编码不能为空")
    private String apiCode;

    @ApiModelProperty(value = "接口用途：0上传，1转化")
    @NotNull(message = "接口用途不能为空")
    private Integer dataType;

    @ApiModelProperty(value = "接口类型：0通用,1定制,2FTP")
    @NotNull(message = "接口类型不能为空")
    private Integer acceptType;

    @ApiModelProperty(value = "文件类型：13:上传清洗周期文件,14:转化清洗周期文件")
    private Integer fileType;

    @ApiModelProperty(value = "文件路径")
    private String sftpPath;



}
