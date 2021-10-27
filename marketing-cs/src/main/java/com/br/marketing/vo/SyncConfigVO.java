package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class SyncConfigVO {

    @ApiModelProperty(value = "主键id")
    private Long id;

    @ApiModelProperty(value = "apiCode")
    private String apiCode;

    @ApiModelProperty(value = "源目录")
    private String srcPath;

    @ApiModelProperty(value = "目的目录")
    private String targePath;

}
