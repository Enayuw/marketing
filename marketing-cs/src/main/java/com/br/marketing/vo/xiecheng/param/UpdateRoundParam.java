package com.br.marketing.vo.xiecheng.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(description = "携程撞库规则修改包轮次")
public class UpdateRoundParam implements Serializable {

    private static final long serialVersionUID = -7211270578842847705L;
    @Schema(description = "包主键id")
    private Long pkgId;

    @Schema(description = "数据包名称")
    private String packageName;

    @Schema(description = "是否开启轮次")
    private Integer round;

}
