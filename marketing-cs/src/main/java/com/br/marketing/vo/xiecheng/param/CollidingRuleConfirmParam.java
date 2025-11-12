package com.br.marketing.vo.xiecheng.param;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "携程撞库规则 确认/暂存 参数")
public class CollidingRuleConfirmParam implements Serializable {

    private static final long serialVersionUID = 1270257474084816056L;

    @Schema(description = "主键id")
    private Long prsId;

    @Schema(description = "商户编号")
    private String apiCode;

    @Schema(description = "携程撞库包的id")
    private Long packageId;

    @Schema(description = "撞库数据清洗任务id")
    private Long collidingDataTaskId;

    @Schema(description = "撞得量级")
    private Integer collidingBackNumber;

    @Schema(description = "撞库开始时间")
    private String collidingStartTime;

    @Schema(description = "撞库结束时间")
    private String collidingEndTime;

    @Schema(description = "一天内的撞库次数")
    private Integer collidingTimes;

    @Schema(description = "一天内撞库时间点")
    private String startTimes;
}
