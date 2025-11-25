package com.br.marketing.dto.tccpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TcCpDataPackageGenDTO {

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

    @Schema(description = "数据包名称")
    @NotNull(message = "数据包名称不能为空")
    private String packageName;
}
