package com.br.marketing.dto.sanliuling.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName CustomerInformationDTO
 * @Author kongbx
 * @Date 2025/8/28 14:41
 */
@Data
public class CustomerInformationDTO implements Serializable {

    @ApiModelProperty("客户号")
    @JsonProperty("applicationId")
    private String applicationId;
    @ApiModelProperty("客户手机号码 加密 AES加密")
    @JsonProperty("phone")
    private String phone;
    @ApiModelProperty("语音合成参数Json字符串，如：{\"变量1\":\"xxx\",\"变量2\":\"xxx\"}")
    @JsonProperty("speechParamSet")
    private String speechParamSet;
    @ApiModelProperty("客户姓名 加密 AES加密")
    @JsonProperty("customerName")
    private String customerName;
    @ApiModelProperty("案件号")
    @JsonProperty("caseCode")
    private String caseCode;
    @ApiModelProperty("产品类型")
    @JsonProperty("productType")
    private String productType;
    @ApiModelProperty("催收名义")
    @JsonProperty("prologueRemark")
    private String prologueRemark;
    @ApiModelProperty("br1, br2, lxr1, lxr2, bn1, bn2")
    @JsonProperty("phoneLabel")
    private String phoneLabel;
}
