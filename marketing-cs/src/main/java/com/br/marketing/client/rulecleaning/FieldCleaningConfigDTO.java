package com.br.marketing.client.rulecleaning;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 字段清洗配置DTO
 * @author guangxiu.li
 * @date 2025/5/10
 */
@Data
@ApiModel(value = "字段清洗配置DTO", description = "字段清洗配置传输对象")
public class FieldCleaningConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "API编码")
    private String apiCode;

    @ApiModelProperty(value = "数据类型：0上传，1转化")
    private Integer dataType;

    @ApiModelProperty(value = "接口类型：0通用,1定制,2FTP")
    private Integer acceptType;

    @ApiModelProperty(value = "字段类型：0-请求层字段，1-衍生字段")
    private Integer fieldType;

    @ApiModelProperty(value = "清洗字段（接口字段）")
    private String cleanField;

    @ApiModelProperty(value = "字段样例")
    private String fieldSample;

    @ApiModelProperty(value = "映射字段（关联字段）")
    private String mappingField;

    @ApiModelProperty(value = "是否需要映射（是否需要清洗）：0-否，1-是")
    private Boolean isMapping;

    @ApiModelProperty(value = "映射规则（清洗规则）")
    private String mappingRule;
}
