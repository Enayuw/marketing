package com.br.marketing.vo.dataclean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DataCleanConfigVO {

    /**
     *
     */
    @Schema(description = "主键id")
    private Long id;

    /**
     * 商户编号
     */
    @Schema(description = "apiCode")
    private String apiCode;


    /**
     * 文件类型  0:上传文件 1:转化文件
     */
    @Schema(description = "文件类型  0:上传文件 1:转化文件")
    private Integer fileType;

    /**
     * 规则名
     */
    @Schema(description = "规则名")
    private String ruleName;

    /**
     * 规则配置展示
     */
    @Schema(description = "规则配置展示")
    private String fieldConfigShow;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private String updateTime;


}
