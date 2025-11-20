package com.br.marketing.dto.tccpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class TcCpDataPackageGenDTO {

    @ApiModelProperty(value = "跑分文件批次号")
    @NotEmpty(message = "跑分文件批次号不能为空")
    private List<String> batchNumberList;

    @ApiModelProperty(value = "查询规则")
    @NotNull(message = "查询规则不能为空")
    @JsonProperty("mRuleCondition")
    private String mRuleCondition;

    @ApiModelProperty(value = "数据包名称")
    @NotNull(message = "数据包名称不能为空")
    private String packageName;
}
