package com.br.marketing.dto.dataclean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * 数据清洗规则组装VO
 *
 * @author zhen.li1
 * @dateTime 2024/05/22 17:49
 */
@Data
public class DataCleanRuleDetailDTO {

    /**
     * 文件ID集合，多个,分割
     */
    @ApiModelProperty(value = "文件ID集合，多个,分割")
    @NotEmpty
    private String fileIds;

    /**
     * apiCode
     */
    @ApiModelProperty(value = "apiCode")
    @NotEmpty
    private String apiCode;


    /**
     * 组装对象
     */
    @ApiModelProperty(value = " 组装对象")
    private String ruleCondition;

    /**
     * 规则名
     */
    @ApiModelProperty(value = "规则名")
    private String ruleName;

    /**
     * 规则ID
     */
    @ApiModelProperty(value = "规则ID")
    private Long ruleId;


    /**
     * 文件类型：0上传，1转化
     */
    @ApiModelProperty(value = "文件类型：0上传，1转化")
    @NotEmpty
    private Integer fileType;

    /**
     * 任务ID
     */
    @ApiModelProperty(value = "任务Id")
    private Long Id;


}
