package com.br.marketing.innerapi.dto.tc;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "同程易融 sync_record apiCode 补齐入参（兼容灵霄回调 JSON 中的扩展字段）")
public class TcyrApiCodeFillRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "批次号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String batchNo;

    @Schema(description = "灵霄/运营回调的 apiCode（与 selectedApiCode 等价）", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonAlias("selectedApiCode")
    private String apiCode;
}
