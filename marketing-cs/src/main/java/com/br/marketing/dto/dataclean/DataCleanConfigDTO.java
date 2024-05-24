package com.br.marketing.dto.dataclean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
/**
 * 数据清洗配置VO
 *
 * @author zhen.li1
 * @dateTime 2024/05/23 17:49
 */
@Data
public class DataCleanConfigDTO {


    @ApiModelProperty(value = "条件id")
    private Long id;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @ApiModelProperty(value = "规则配置")
    private String ruleConfig;

    /**
     * 文件类型：0上传，1转化
     */
    @ApiModelProperty(value = "文件类型：0上传，1转化")
    private Integer fileType;

    /**
     * apiCode
     */
    @ApiModelProperty(value = "apiCode")
    private String apiCode;

}
