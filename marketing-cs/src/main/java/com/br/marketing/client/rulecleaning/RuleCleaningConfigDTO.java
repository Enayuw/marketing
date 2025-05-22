package com.br.marketing.client.rulecleaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 规则清洗配置DTO
 * @author guangxiu.li
 * @date 2025/5/10
 */
@Data
@ApiModel(value = "规则清洗配置DTO", description = "规则与清洗规则配置传输对象")
public class RuleCleaningConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "API编码")
    @NotNull(message = "API编码不能为空")
    private String apiCode;

    @ApiModelProperty(value = "数据类型：0上传，1转化")
    @NotNull(message = "数据类型不能为空")
    private Integer dataType;

    @ApiModelProperty(value = "接口类型：0通用,1定制,2FTP")
    @NotNull(message = "接口类型不能为空")
    private Integer acceptType;

    @ApiModelProperty(value = "清洗配置")
    private List<FieldCleaningConfigDTO> cleaningConfig;
} 