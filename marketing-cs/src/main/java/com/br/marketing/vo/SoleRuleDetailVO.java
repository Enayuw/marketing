package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * 页面新增去重规则 请求参数
 */
@Data
public class SoleRuleDetailVO {

    @Schema(description = "去重规则id")
    private String soleId;

    @Schema(description = "去重规则名称")
    @NotBlank(message = "名称必填")
    @Length(min = 1, max = 40, message = "规则名称长度不合法")
    private String soleName;

    @Schema(description = "去重字段")
    @NotBlank(message = "字段必选")
    private String soleFields;

    @Schema(description = "去重时间周期")
    @Min(value = 0)
    @Max(value = 180)
    private Integer soleCycleTimes;

    @Schema(description = "匹配商户列表")
    @NotEmpty(message = "匹配商户必选")
    private List<CustUserTypeSelectVO> soleCustom;
}
