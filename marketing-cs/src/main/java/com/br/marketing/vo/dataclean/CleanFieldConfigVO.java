package com.br.marketing.vo.dataclean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CleanFieldConfigVO {

    /**
     * 主键ID
     */
    @Schema(description = "id")
    private Long id;


    /**
     * 数据类型：0:上传，1:转化
     */
    @Schema(description = "数据类型：0:上传，1:转化")
    private Integer dataType;

    /**
     * 接收类型：0:通用,1:定制,2:FTP
     */
    @Schema(description = "接收类型：0:通用,1:定制,2:FTP")
    private Integer acceptType;

    /**
     * 字段集合，多个字段用,分割
     */
    @Schema(description = "字段集合，多个字段用,分割")
    private String fieldCollect;

    /**
     * 数据类型：0:上传，1:转化
     */
    @Schema(description = "数据源类型：0:营销中台，1:外呼系统")
    private Integer systemType;

}
