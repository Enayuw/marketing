package com.br.marketing.dto.tccpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TcCpDataCleanTaskDTO {

    @ApiModelProperty(value = "数据包id列表")
    @NotEmpty(message = "数据包id列表不能为空")
    private List<String> packageList;

    @ApiModelProperty(value = "扩展字段")
    @JsonProperty("mRuleCondition")
    private String extend;

}
