package com.br.marketing.dto.smy.request;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;

@Data
public class NameValueDTO implements Serializable {
    private static final long serialVersionUID = -6958499637324020866L;
    @Schema(description = "32位小写md5加密客户号")
    @JsonProperty("cid_md5")
    private String cidMd5;
    @Schema(description = "32位小写md5加密手机号")
    @JsonProperty("mid_md5")
    private String midMd5;
    @Schema(description = "注册时间")
    @JsonProperty("register_datetime")
    private String registerDateTime;
    @Schema(description = "json扩展字段 注：建议全部存储")
    @JsonProperty("extend_fields")
    private JSONObject extendFields;
}
