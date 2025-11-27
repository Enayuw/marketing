package com.br.marketing.vo.xiecheng.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "修改撞库规则参数")
public class UpdateCollidingRuleParam {

    @Schema(description = "主键id")
    private Long dprId;

    @Schema(description = "数据包名称")
    private String packageName;

    @Schema(description = "原-设定撞得量级")
    private Integer originalCollidingBackNumber;

    @Schema(description = "原-一天内的撞库次数")
    private Integer originalCollidingTimes;

    @Schema(description = "原-开启撞库时间 yyyy-MM-dd HH:mm:ss")
    private String originalCollidingStartTime;

    @Schema(description = "原-结束撞库时间 yyyy-MM-dd HH:mm:ss")
    private String originalCollidingEndTime;

    @Schema(description = "设定撞得量级")
    private Integer collidingBackNumber;

    @Schema(description = "一天内的撞库次数")
    private Integer collidingTimes;

    @Schema(description = "开启撞库时间 yyyy-MM-dd HH:mm:ss")
    private String collidingStartTime;

    @Schema(description = "结束撞库时间 yyyy-MM-dd HH:mm:ss")
    private String collidingEndTime;

    @Schema(description = "撞库开始时间（多个时间以逗号分割，格式HH:mm）")
    private String startTimes;

}
