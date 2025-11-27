package com.br.marketing.vo.xiecheng.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "携程撞库规则启用禁用修改参数")
public class UpdateCollidingSwitchParam implements Serializable {

    private static final long serialVersionUID = -2438627107688674264L;
    @Schema(description = "规则主键id")
    private Long dprId;

    @Schema(description = "数据包名称")
    private String packageName;

    @Schema(description = "任务状态")
    private Integer collidingSwitch;
}
