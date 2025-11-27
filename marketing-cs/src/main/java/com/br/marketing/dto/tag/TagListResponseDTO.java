package com.br.marketing.dto.tag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 标签列表响应DTO
 */
@Data
@Schema(description = "标签列表响应DTO")
public class TagListResponseDTO {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "标签编码")
    private String tagCode;

    @Schema(description = "标签名称")
    private String tagName;

    @Schema(description = "标签规则总结")
    private String summary;

    @Schema(description = "标签规则内容")
    private String content;

    @Schema(description = "预估人数")
    private Integer tagNumber;

    @Schema(description = "数据源编码")
    private String sourceCode;

    @Schema(description = "API范围，分号分隔")
    private String apiCodeScope;

    @Schema(description = "API授权，分号分隔")
    private String apiCodeLicense;

    @Schema(description = "状态：1-启用 0-禁用")
    private Integer status;

    @Schema(description = "创建人")
    private String creator;

    @Schema(description = "创建人ID")
    private Long creatorId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "是否可编辑")
    private Boolean canEdit;

    @Schema(description = "是否可删除")
    private Boolean canDelete;
}