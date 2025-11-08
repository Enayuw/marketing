package com.br.marketing.vo.xiecheng.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Schema(description = "携程撞库规则优先级修改参数")
public class UpdatePriorityParam implements Serializable {

    private static final long serialVersionUID = -7211270578842847705L;
    @ApiModelProperty("包主键id")
    private Long pkgId;

    @ApiModelProperty("数据包名称")
    private String packageName;

    @ApiModelProperty("优先级")
    private Integer priority;

    @ApiModelProperty("原-优先级")
    private Integer originalPriority;

}
