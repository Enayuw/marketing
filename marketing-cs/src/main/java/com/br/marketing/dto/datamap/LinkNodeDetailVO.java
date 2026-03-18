package com.br.marketing.dto.datamap;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 链路节点详情 VO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@Schema(description = "链路节点详情")
public class LinkNodeDetailVO {

    @Schema(description = "link_node 表的ID")
    private Long id;

    @Schema(description = "链路ID")
    private Long linkId;

    @Schema(description = "节点id 前端生成")
    private Long nodeId;

    @Schema(description = "节点字典ID")
    private Long nodeDictId;

    @Schema(description = "节点别名")
    private String nodeAlias;

    @Schema(description = "状态（0-禁用 1-启用）")
    private Integer status;

    @Schema(description = "边类型：SOLID-实线(必须) DASHED-虚线(可选)")
    private String edgeType;

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

    @Schema(description = "节点代码")
    private String nodeCode;

    @Schema(description = "apiCode")
    private String apiCode;

    @Schema(description = "节点类型")
    private String nodeType;

    @Schema(description = "节点名称")
    private String nodeName;
}


