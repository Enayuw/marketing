package com.br.marketing.client.biocloo.input;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BlackDataRequestDTO {

    @ApiModelProperty(value = "apiCode")
    private String apiCode;
    @ApiModelProperty(value = "加密后BlackDataDTO")
    private String jsonData;
}
