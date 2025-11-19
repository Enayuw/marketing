package com.br.marketing.dto.rulecenter;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class XcCycleDeleteDTO {

    @ApiModelProperty(value = "商户编号")
    @NotNull(message = "商户编号不能为空")
    private String apiCode;

    @ApiModelProperty(value = "跑分文件批次号")
    @NotEmpty(message = "跑分文件批次号不能为空")
    private List<String> batchNumberList;

    @ApiModelProperty(value = "查询规则")
    @NotEmpty(message = "查询规则不能为空")
    @JsonProperty("mRuleCondition")
    private String mRuleCondition;

    @ApiModelProperty(value = "剔除量级分布信息")
    @NotEmpty(message = "剔除量级分布信息不能为空")
    private List<XcDeleteMagnitudeDistDTO> deleteMagnitudeDistList;
}
