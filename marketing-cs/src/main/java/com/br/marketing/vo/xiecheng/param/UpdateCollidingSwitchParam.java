package com.br.marketing.vo.xiecheng.param;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel(value = "携程撞库规则启用禁用修改参数")
public class UpdateCollidingSwitchParam implements Serializable {

    private static final long serialVersionUID = -2438627107688674264L;
    @ApiModelProperty("规则主键id")
    private Long dprId;

    @ApiModelProperty("任务状态")
    private Integer collidingSwitch;
}
