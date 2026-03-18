package com.br.marketing.dto.datamap;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 链路信息 VO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@Schema(description = "链路信息")
public class LinkInfoVO {
    
    @Schema(description = "链路ID")
    private Long id;

    @Schema(description = "apiCode")
    private String apiCode;
    
    @Schema(description = "链路代码")
    private String linkCode;

    @Schema(description = "链路名称")
    private String linkName;

    @Schema(description = "业务场景")
    private String bizScene;

    @Schema(description = "链路描述")
    private String description;

    @Schema(description = "链路图结构（JSON格式，包含节点、连线、位置等完整信息）")
    private String graphJson;

    @Schema(description = "状态（0-禁用 1-启用）")
    private Byte status;

    @Schema(description = "来源类型：MANUAL-手动创建 AUTO-自动发现")
    private String sourceType;

    @Schema(description = "匹配的模板ID")
    private String templateId;

    @Schema(description = "自动匹配时间")
    private Date matchTime;

    @Schema(description = "总调用次数")
    private Long totalCount;

    @Schema(description = "总数据量级")
    private Long totalMagnitude;

    @Schema(description = "首次更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime firstUpdateTime;

    @Schema(description = "最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastUpdateTime;

    @Schema(description = "更新次数")
    private Integer updateCount;

    @Schema(description = "创建时间")
    private Date createdTime;

    @Schema(description = "更新时间")
    private Date updatedTime;
}


