package com.br.marketing.client.rulecleaning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 字段清洗配置DTO
 * @author guangxiu.li
 * @date 2025/5/10
 */
@Data
@Schema(description = "字段清洗配置DTO")
public class FieldCleaningConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "API编码")
    @NotNull(message = "API编码不能为空")
    private String apiCode;

    @Schema(description = "数据类型：0上传，1转化")
    @NotNull(message = "数据类型不能为空")
    private Integer dataType;

    @Schema(description = "接口类型：0通用,1定制,2FTP")
    @NotNull(message = "接口类型不能为空")
    private Integer acceptType;

    @Schema(description = "字段类型：0-请求层字段，1-衍生字段")
    private Integer fieldType;

    @Schema(description = "清洗字段（接口字段）")
    private String cleanField;

    @Schema(description = "清洗字段层级")
    private Integer level;

    @Schema(description = "父节点完整路径")
    private String parentPath;

    @Schema(description = "字段样例")
    private String fieldSample;

    @Schema(description = "映射字段（关联字段）")
    private String mappingField;

    @Schema(description = "是否需要映射（是否需要清洗）：0-否，1-是")
    private Boolean isMapping;

    @Schema(description = "映射规则（清洗规则）")
    private String mappingRule;

    @Schema(description = "数据源类型：0:营销中台，1:外呼系统")
    private Integer systemType;
}
