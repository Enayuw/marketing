package com.br.marketing.dto.customer;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @ClassName SmsRecordDTO
 * @Description 短信回调
 * @Author kongbx
 * @Date 2024/9/25 10:41
 */
@Data
public class SmsRecordDTO {

    @ApiModelProperty(value = "商户编号")
    private String api_code;

    @ApiModelProperty(value = "公司标识")
    private String cid;

    @ApiModelProperty(value = "短信流水号")
    private String thirdCallNo;

    @ApiModelProperty(value = "短信发送状态")
    private Integer smsSendStatus;

    @ApiModelProperty(value = "案件编号")
    private String caseNum;

    @ApiModelProperty(value = "预留字段1")
    private String reserveField1;

}
