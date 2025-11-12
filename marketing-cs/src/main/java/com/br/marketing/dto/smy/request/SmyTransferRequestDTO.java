package com.br.marketing.dto.smy.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SmyTransferRequestDTO implements Serializable {
    private static final long serialVersionUID = -3120269378878940556L;
    @Schema(description = "事件类型 例：登陆：login; 注册：regist; 人脸识别：F1; 身份认证：F2; 填联系人：F3; 完件：finish; 授信成功：approve; 交易成功：loan; 客诉名单：blacklist")
    @JsonProperty("event_type")
    private String eventType;
    @Schema(description = "事件发生的毫秒级时间戳 例：1694597797924")
    @JsonProperty("event_time")
    private Long eventTime;
    @Schema(description = "回传标识 注：最长为256")
    @JsonProperty("cid")
    private String cid;
    @Schema(description = "扩展json字段 注：JSON字符串 例：假完件标识：finish_fake; API完件标识：finish_api; 授信额度区间：acct_level")
    @JsonProperty("extend_fields")
    private String extendFields;
}
