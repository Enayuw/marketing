package com.br.marketing.client.biocloo.input;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BlackDataRequestDTO {

    @ApiModelProperty(value = "请求类型:固定值blackData")
    private String method;
    @ApiModelProperty(value = "apiCode")
    private String apiCode;
    @ApiModelProperty(value = "黑名单列表")
    private List<BlackDataDTO> data;
}
