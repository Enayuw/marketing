package com.br.marketing.vo.xiecheng;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "携程撞库暂存规则VO")
public class XiechengCollidingStagingRuleVO implements Serializable {
    private static final long serialVersionUID = -6620381451286081664L;

    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "ApiCode")
    private String apiCode;

    @Schema(description = "携程撞库包的id")
    private Long packageId;

    @Schema(description = "撞库数据清洗任务id")
    private Long collidingDataTaskId;

    @Schema(description = "撞得量级")
    private Integer collidingBackNumber;

    @Schema(description = "撞库开始时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date collidingStartTime;

    @Schema(description = "撞库结束时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date collidingEndTime;

    @Schema(description = "一天内的撞库次数")
    private Integer collidingTimes;

    @Schema(description = "创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @Schema(description = "更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    @Schema(description = "是否删除 0 正常，1删除")
    private Integer isDelete;

    @Schema(description = "撞库开始时间（多个时间以逗号分割，格式HH:mm）")
    private String startTimes;
}
