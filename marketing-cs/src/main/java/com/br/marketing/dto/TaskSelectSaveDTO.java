package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class TaskSelectSaveDTO {
    @Schema(description = "apiCode")
    @NotNull(message = "apiCode不能为空")
    private String apiCode;

    @Schema(description = "跑分日期")
    @NotNull(message = "跑分日期不能为空")
    private String taskDate;

    @Schema(description = "跑分时间")
    @NotNull(message = "跑分时间不能为空")
    private String taskTime;

    @Schema(description = "数据id")
    @NotNull(message = "数据id不能为空")
    @Size(min = 1,message = "数据id不能为空")
    private List<Long> dataIdDesc;

    @Schema(description = "规则id")
    @NotNull(message = "规则id不能为空")
    @Size(min = 1,message = "规则id不能为空")
    private List<Long> ruleIds;

    @Schema(description = "数据条数 大于500无效")
    @Max(value = 500,message = "验证条数不能大于500")
    private Integer dataLimit;

    @Schema(description = "是否规则验证 1-验证；不传或者其他值不做处理")
    private Integer isOrNoScoreVer;

    @Schema(description = "taskCreateMethod")
    private String taskCreateMethod;

    @Schema(description = "标签名称")
    private String labelName;
}
