package com.br.marketing.client.biocloo.input;

import java.util.List;

import com.br.marketing.rule.SourceData;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class BlackDataDTO extends SourceData {
    @Schema(description = "请求类型:固定值blackData")
    private String method;
    @Schema(description = "apiCode")
    private String apiCode;
    @Schema(description = "黑名单列表")
    private List<DataDTO> data;
}
