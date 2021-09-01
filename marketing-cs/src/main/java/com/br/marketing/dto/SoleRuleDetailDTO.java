package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class SoleRuleDetailDTO {

    @ApiModelProperty(value = "去重规则id")
    private Long id;

    @NotBlank(message = "名称必填")
    @ApiModelProperty(value = "去重规则名称")
    private String soleName;

    @NotBlank(message = "字段必选")
    @ApiModelProperty(value = "去重字段")
    private String soleFields;

    @NotNull(message = "去重周期必填")
    @ApiModelProperty(value = "去重时间周期")
    private String soleCycleTimes;

    @ApiModelProperty(value = "匹配商户")
    private List<Map> soleCustom;







}
