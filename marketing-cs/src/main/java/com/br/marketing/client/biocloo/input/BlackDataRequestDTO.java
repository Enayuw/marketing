package com.br.marketing.client.biocloo.input;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BlackDataRequestDTO {

    @Schema(description = "apiCode")
    private String apiCode;
    @Schema(description = "加密后BlackDataDTO")
    private String jsonData;
}
