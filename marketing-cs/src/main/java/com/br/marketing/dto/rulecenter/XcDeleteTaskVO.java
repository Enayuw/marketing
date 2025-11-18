package com.br.marketing.dto.rulecenter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "携程剔除任务VO")
public class XcDeleteTaskVO {

    @Schema(description = "规则主键id")
    private Long dprId;

    @Schema(description = "客户编号")
    private String cid;

    @Schema(description = "ApiCode")
    private String apiCode;

    @Schema(description = "客户名称")
    private String shortName;

    @Schema(description = "预估剔除量级")
    private String discreetNumber;

    @Schema(description = "实际剔除量级")
    private String actualNumber;

    @Schema(description = "releaseTime开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeBegin;

    @Schema(description = "releaseTime结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeEnd;

    @Schema(description = "剔除类型")
    private Integer taskType;

    @Schema(description = "剔除任务执行时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime taskExecuteTime;

    @Schema(description = "任务执行状态")
    private Integer taskStatus;

    @Schema(description = "创建时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime createTime;

    @Schema(description = "修改时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updateTime;
}
