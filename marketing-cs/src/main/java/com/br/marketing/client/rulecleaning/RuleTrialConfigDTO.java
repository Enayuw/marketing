package com.br.marketing.client.rulecleaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(value = "试跑规则配置DTO", description = "试跑规则配置传输对象")
public class RuleTrialConfigDTO {

    @ApiModelProperty(value = "API编码")
    @NotNull(message = "API编码不能为空")
    private String apiCode;

    @ApiModelProperty(value = "查询日期")
    @NotNull(message = "查询日期不能为空")
    private String appletDate;

    @ApiModelProperty(value = "账号类型")
    @NotNull(message = "账号类型不能为空")
    private String accountType;

    @ApiModelProperty(value = "数据类型：0上传，1转化")
    @NotNull(message = "数据类型不能为空")
    private Integer dataType;

    @ApiModelProperty(value = "接口类型：0通用,1定制,2FTP")
    @NotNull(message = "接口类型不能为空")
    private Integer acceptType;

    @ApiModelProperty(value = "数据实际条数")
    @NotNull(message = "数据实际条数不能为空")
    private Integer actualNum;

    @ApiModelProperty(value = "sftp地址")
    private String sftpPath;

}
