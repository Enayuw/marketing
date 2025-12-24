package com.br.marketing.client.rulecleaning;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 规则清洗配置DTO
 * @author guangxiu.li
 * @date 2025/5/10
 */
@Data
@Schema(description = "规则清洗配置DTO")
public class RuleCleaningConfigDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "API编码")
    private String apiCode;

    @Schema(description = "数据类型：0上传，1转化")
    private Integer dataType;

    @Schema(description = "接口类型：0通用,1定制,2FTP")
    private Integer acceptType;

    @Schema(description = "清洗配置")
    private List<FieldCleaningConfigDTO> cleaningConfig;

    @Schema(description = "配置ID")
    private Long configId;

    @Schema(description = "数据源类型：0:营销中台，1:外呼系统")
    private Integer systemType;

} 