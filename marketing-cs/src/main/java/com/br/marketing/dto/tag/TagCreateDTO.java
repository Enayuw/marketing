package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 标签创建DTO
 */
@Data
@ApiModel(description = "标签创建请求DTO")
public class TagCreateDTO {
    @NotBlank(message = "标签名称不能为空")
    @ApiModelProperty(value = "标签名称", required = true)
    private String tagName;

    @NotNull(message = "条件树不能为空")
    @ApiModelProperty(value = "条件树配置", required = true)
    private TagConditionTreeDTO conditionTree;

    @NotBlank(message = "时间范围不能为空")
    @ApiModelProperty(value = "时间范围(YESTERDAY-昨天,LAST_THREE_DAYS-最近三天,LAST_WEEK-最近一周,LAST_MONTH-最近一月,LAST_THREE_MONTHS-最近三月)", required = true)
    private String timeRange;

    @NotEmpty(message = "标签范围不能为空")
    @ApiModelProperty(value = "标签统计范围APICode列表", required = true)
    private List<String> scopeApiCodes;

    @NotEmpty(message = "授权APICode不能为空")
    @ApiModelProperty(value = "标签授权APICode列表", required = true)
    private List<String> authorizedApiCodes;
}