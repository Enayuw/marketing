package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;


/**
 * 场景字段配置列表返回
 *
 * @author songjuanjuan
 * @dateTime 2021/10/21 17:49
 */
@Data
@ApiModel(value = "场景字段配置表")
public class VariableDicListVO{

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * 商户
     */
    @ApiModelProperty(value = "商户")
    @NotEmpty
    private String cid;

    /**
     * 用户编号
     */
    @ApiModelProperty(value = "用户编号")
    @NotEmpty
    private String apiCode;

    /**
     * 字段名称
     */
    @ApiModelProperty(value = "字段名称")
    @NotEmpty
    private String fieldName;

    /**
     * 字段值
     */
    @ApiModelProperty(value = "字段值")
    @NotEmpty
    private String fieldValue;

    /**
     * 字段描述
     */
    @ApiModelProperty(value = "字段描述")
    private String fieldDesc;

    /**
     * 默认有效期是N天，代表T+N范围
     */
    @ApiModelProperty(value = "默认有效期是N天，代表T+N范围")
    private String validDaysDefault;

    /**
     * 删除标志；1-正常；9-删除；
     */
    @ApiModelProperty(value = "删除标志(1:正常;9:删除,默认1)")
    private Integer isDel;

    /**
     * 入库时间
     */
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private String updateTime;
}
