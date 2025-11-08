package com.br.marketing.client.rulecleaning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "试跑规则配置DTO")
public class RuleTrialConfigDTO {

    @Schema(description = "API编码")
    @NotNull(message = "API编码不能为空")
    private String apiCode;

    @Schema(description = "查询日期")
    @NotNull(message = "查询日期不能为空")
    private String appletDate;

    @Schema(description = "账号类型")
    @NotNull(message = "账号类型不能为空")
    private String accountType;

    @Schema(description = "数据类型：0上传，1转化")
    @NotNull(message = "数据类型不能为空")
    private Integer dataType;

    @Schema(description = "接口类型：0通用,1定制,2FTP")
    @NotNull(message = "接口类型不能为空")
    private Integer acceptType;

    @Schema(description = "数据实际条数")
    @NotNull(message = "数据实际条数不能为空")
    private Integer actualNum;

    @Schema(description = "sftp地址")
    private String sftpPath;

}
