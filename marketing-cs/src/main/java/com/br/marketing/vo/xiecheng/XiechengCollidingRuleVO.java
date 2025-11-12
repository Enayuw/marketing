package com.br.marketing.vo.xiecheng;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "携程撞库规则VO")
public class XiechengCollidingRuleVO implements Serializable {
    private static final long serialVersionUID = 6038761827928494339L;

    @Schema(description = "规则主键id")
    private Long dprId;

    @Schema(description = "客户编号")
    private String cid;

    @Schema(description = "ApiCode")
    private String apiCode;

    @Schema(description = "客户名称")
    private String shortName;

    @Schema(description = "包主键id")
    private Long pkgId;

    @Schema(description = "数据包名称")
    private String packageName;

    @Schema(description = "预估量级")
    private String discreetNumber;

    @Schema(description = "实际可用量级")
    private String remainingNumber;

    @Schema(description = "任务状态")
    private Integer collidingSwitch;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "轮次")
    private Integer round;

    @Schema(description = "设定撞得量级")
    private Integer collidingBackNumber;

    @Schema(description = "每日撞库次数")
    private Integer collidingTimes;

    @Schema(description = "撞库开始时间（多个时间以逗号分割，格式HH:mm）")
    private String startTimes;

    @Schema(description = "数据清洗时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskStartTime;

    @Schema(description = "开启撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date collidingStartTime;

    @Schema(description = "结束撞库时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date collidingEndTime;

    @Schema(description = "创建时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "修改时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
