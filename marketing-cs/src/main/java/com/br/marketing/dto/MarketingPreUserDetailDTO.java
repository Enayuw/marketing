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

    private String cellMd5;

    private String cellSha256;

    @ApiModelProperty(value = "身份证号")
    private String id;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "场景：促首登、促申完、促动之")
    private String groupType;

    @ApiModelProperty(value = "用户唯一编号，回调时用到")
    @NotNull(message = "custNum必传")
    @NotEmpty(message = "custNum必传")
    private String custNum;

    @ApiModelProperty(value = "")
    private String registerDate;

    @ApiModelProperty(value = "业务保留字段1")
    private String reserveField1;

    @ApiModelProperty(value = "业务保留字段2")
    private String reserveField2;

    @ApiModelProperty(value = "执行日期")
    private String appletDate;

    @ApiModelProperty(value = "类型 MD5、Sha256")
    private String failType;

    @ApiModelProperty(value = "预留剔除状态字段 1：正常，2：剔除")
    private Integer status;

    private String cusBatch;
}
