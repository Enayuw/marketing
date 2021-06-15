package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class MarketingPreUserDetailDTO implements Serializable {
    private static final long serialVersionUID = 1;

    @ApiModelProperty(value = "手机号")
    @NotNull(message = "cell必传")
    @NotEmpty(message = "cell必传")
    private String cell;

    @ApiModelProperty(value = "场景：促首登、促申完、促动之")
    private String groupType;

    @ApiModelProperty(value = "用户唯一编号，回调时用到")
    @NotNull(message = "caseNum必传")
    @NotEmpty(message = "caseNum必传")
    private String caseNum;

    @ApiModelProperty(value = "")
    private String registerDate;

    @ApiModelProperty(value = "业务保留字段1")
    private String reserveField1;

    @ApiModelProperty(value = "业务保留字段2")
    private String reserveField2;
}
