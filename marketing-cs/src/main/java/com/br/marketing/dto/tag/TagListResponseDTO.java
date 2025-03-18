package com.br.marketing.dto.tag;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 标签列表响应DTO
 */
@Data
@ApiModel("标签列表响应DTO")
public class TagListResponseDTO {

    @ApiModelProperty("ID")
    private Long id;

    @ApiModelProperty("标签编码")
    private String tagCode;

    @ApiModelProperty("标签名称")
    private String tagName;

    @ApiModelProperty("规则摘要")
    private String summary;

    @ApiModelProperty("标签人数")
    private Integer tagNumber;

    @ApiModelProperty("数据源范围")
    private String apiCodeScope;

    @ApiModelProperty("数据源授权")
    private String apiCodeLicense;

    @ApiModelProperty("状态")
    private Integer status;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}