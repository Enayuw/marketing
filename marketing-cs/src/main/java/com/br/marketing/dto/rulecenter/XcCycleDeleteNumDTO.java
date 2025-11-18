package com.br.marketing.dto.rulecenter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class XcCycleDeleteNumDTO {

    @Schema(description = "商户编号")
    @NotNull(message = "商户编号不能为空")
    private String apiCode;

    @Schema(description = "跑分文件批次号")
    @NotEmpty(message = "跑分文件批次号不能为空")
    private List<String> batchNumberList;

    @Schema(description = "查询规则")
    @NotNull(message = "查询规则不能为空")
    @JsonProperty("mRuleCondition")
    private String mRuleCondition;

    @Schema(description = "releaseTime开始时间")
    @NotNull(message = "releaseTime开始时间不能为空")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private LocalDateTime releaseTimeBegin;

    @Schema(description = "releaseTime结束时间")
    @NotNull(message = "releaseTime结束时间不能为空")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone="Asia/Shanghai")
    private LocalDateTime releaseTimeEnd;
}
