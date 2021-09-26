package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 列表展示跑分配置
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/8/31 14:33
 */
@Setter
@Getter
@NoArgsConstructor
@ApiModel(value = "跑分配置", description = "展示跑分配置VO")
public class ScoreRuleConfigPageVO {

    /**
     * 2021/8/31 16:11 规则主键
     */
    @ApiModelProperty(value = "规则主键", dataType = "long", position = 0)
    private Long id;

    /**
     * 2021/8/31 16:11 规则与客户关系主键
     */
    @ApiModelProperty(value = "规则与客户关系主键", dataType = "long", position = 1)
    private Long crId;

    /**
     * 2021/8/31 16:11 规则名称
     */
    @ApiModelProperty(value = "规则名称", dataType = "string", position = 2)
    private String ruleName;

    /**
     * 2021/8/31 16:11 合作客户ID
     */
    @ApiModelProperty(value = "合作客户ID", dataType = "string", position = 3)
    private String cid;

    /**
     * 2021/8/31 16:11 接口编码
     */
    @ApiModelProperty(value = "接口编码", dataType = "string", position = 4)
    private String apiCode;

    /**
     * 2021/8/31 16:11 状态
     */
    @ApiModelProperty(value = "状态", dataType = "string", position = 5)
    private String status;

    /**
     * 2021/8/31 16:11 创建时间
     */
    @ApiModelProperty(value = "创建时间", dataType = "string", position = 6)
    private String createTime;

    /**
     * 2021/8/31 16:11 更新时间
     */
    @ApiModelProperty(value = "更新时间", dataType = "string", position = 7)
    private String updateTime;

    /**
     * 规则简拼
     */
    @ApiModelProperty(value = "规则简拼", dataType = "string", position = 8)
    private String ruleNameShort;
}
