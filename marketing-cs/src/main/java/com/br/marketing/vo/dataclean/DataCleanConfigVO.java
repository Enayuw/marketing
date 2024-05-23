package com.br.marketing.vo.dataclean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DataCleanConfigVO {

    /**
     *
     */
    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * 商户编号
     */
    @ApiModelProperty(value = "apiCode")
    private String apiCode;


    /**
     * 文件类型  0:上传文件 1:转化文件
     */
    @ApiModelProperty(value = "文件类型  0:上传文件 1:转化文件")
    private Integer fileType;

    /**
     * 规则名
     */
    @ApiModelProperty(value = "规则名")
    private String ruleName;

    /**
     * 规则配置展示
     */
    @ApiModelProperty(value = "规则配置展示")
    private String field_config_show;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private String updateTime;


}
