package com.br.marketing.dto.datamap;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("链路节点详情")
public class LinkNodeDetailVO {

    @ApiModelProperty("link_node 表的ID")
    private Long id;

    @ApiModelProperty("链路ID")
    private Long linkId;

    @ApiModelProperty("节点id 前端生成")
    private Long nodeId;

    @ApiModelProperty("节点字典ID")
    private Long nodeDictId;

    @ApiModelProperty("节点别名")
    private String nodeAlias;

    @ApiModelProperty("状态（0-禁用 1-启用）")
    private Integer status;

    @ApiModelProperty("总调用次数")
    private Long totalCount;

    @ApiModelProperty("总数据量级")
    private Long totalMagnitude;

    @ApiModelProperty("首次更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime firstUpdateTime;

    @ApiModelProperty("最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastUpdateTime;

    @ApiModelProperty("更新次数")
    private Integer updateCount;

    @ApiModelProperty("节点代码")
    private String nodeCode;

    @ApiModelProperty("apiCode")
    private String apiCode;

    @ApiModelProperty("节点类型")
    private String nodeType;

    @ApiModelProperty("节点名称")
    private String nodeName;
}


