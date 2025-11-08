package com.br.marketing.dto.dataclean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
/**
 * 数据清洗配置VO
 *
 * @author zhen.li1
 * @dateTime 2024/05/23 17:49
 */
@Data
public class DataCleanConfigDTO {


    @Schema(description = "条件id")
    private Long id;

    @Schema(description = "规则名称")
    private String ruleName;

    @Schema(description = "规则配置")
    private String ruleConfig;

    /**
     * 文件类型：0上传，1转化
     */
    @Schema(description = "文件类型：0上传，1转化")
    private Integer fileType;

    /**
     * apiCode
     */
    @Schema(description = "apiCode")
    private String apiCode;

}
