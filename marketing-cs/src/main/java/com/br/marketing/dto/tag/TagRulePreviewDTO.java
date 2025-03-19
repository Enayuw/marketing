package com.br.marketing.dto.tag;

import com.br.marketing.enums.TagTimeRangeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
@ApiModel(description = "标签规则预览DTO")
public class TagRulePreviewDTO {

    @NotBlank(message = "时间范围不能为空")
    @ApiModelProperty(value = "时间范围(YESTERDAY-昨天,LAST_THREE_DAYS-最近三天,LAST_WEEK-最近一周,LAST_MONTH-最近一月,LAST_THREE_MONTHS-最近三月)", required = true)
    private TagTimeRangeEnum timeRange;

    @NotNull(message = "条件树不能为空")
    @ApiModelProperty(value = "条件树配置", required = true)
    private TagConditionTreeDTO conditionTree;
} 