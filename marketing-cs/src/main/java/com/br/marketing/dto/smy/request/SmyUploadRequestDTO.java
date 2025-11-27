package com.br.marketing.dto.smy.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SmyUploadRequestDTO implements Serializable {
    private static final long serialVersionUID = -3120269378878940556L;
    @Schema(description = "请求流水号 注：每个请求唯一")
    @JsonProperty("request_no")
    private String requestNo;
    @Schema(description = "客群标识批次号 注：同个批次的所有请求相同 例：WP_FIN_DT_100_20231010")
    @JsonProperty("case_type")
    private String caseType;
    @Schema(description = "批次总数")
    @JsonProperty("total")
    private Integer total;
    @Schema(description = "代运营名单列表 注：单次上传的名单不大于 100 条")
    @JsonProperty("name_list")
    private List<NameValueDTO> nameList;
}
