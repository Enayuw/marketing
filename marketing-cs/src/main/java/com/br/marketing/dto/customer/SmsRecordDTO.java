package com.br.marketing.dto.customer;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "商户编号")
    private String apiCode;

    @Schema(description = "公司标识")
    private String cid;

    @Schema(description = "场景")
    private String userType;

    @Schema(description = "短信流水号")
    private String thirdCallNo;

    @Schema(description = "短信发送状态")
    private Integer smsSendStatus;

    @Schema(description = "案件编号")
    private String caseNum;

    @Schema(description = "预留字段1")
    private String reserveField1;

    @Schema(description = "回调类型")
    private Integer callBackType;

}
