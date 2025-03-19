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

    @ApiModelProperty("标签规则总结")
    private String summary;

    @ApiModelProperty("预估人数")
    private Integer tagNumber;

    @ApiModelProperty("数据源编码")
    private String sourceCode;

    @ApiModelProperty("API范围，分号分隔")
    private String apiCodeScope;

    @ApiModelProperty("API授权，分号分隔")
    private String apiCodeLicense;

    @ApiModelProperty("状态：1-启用 0-禁用")
    private Integer status;

    @ApiModelProperty("创建人")
    private String creator;

    @ApiModelProperty("创建人ID")
    private Long creatorId;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;

    @ApiModelProperty("是否可编辑")
    private Boolean canEdit;

    @ApiModelProperty("是否可删除")
    private Boolean canDelete;
}