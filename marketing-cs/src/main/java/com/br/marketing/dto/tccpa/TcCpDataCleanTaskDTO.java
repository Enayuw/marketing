package com.br.marketing.dto.tccpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Data
public class TcCpDataCleanTaskDTO {

    @Schema(description = "数据包id列表")
    @NotEmpty(message = "数据包id列表不能为空")
    private List<String> packageList;

    @Schema(description = "扩展字段")
    @JsonProperty("mRuleCondition")
    private String extend;

}
