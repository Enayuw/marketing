package com.br.marketing.vo.dataclean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CleanFieldConfigVO {

    /**
     * 主键ID
     */
    @ApiModelProperty(value = "id")
    private Long id;


    /**
     * 数据类型：0:上传，1:转化
     */
    @ApiModelProperty(value = "数据类型：0:上传，1:转化")
    private Integer dataType;

    /**
     * 接收类型：0:通用,1:定制,2:FTP
     */
    @ApiModelProperty(value = "接收类型：0:通用,1:定制,2:FTP")
    private Integer acceptType;

    /**
     * 字段集合，多个字段用,分割
     */
    @ApiModelProperty(value = "字段集合，多个字段用,分割")
    private String fieldCollect;



}
