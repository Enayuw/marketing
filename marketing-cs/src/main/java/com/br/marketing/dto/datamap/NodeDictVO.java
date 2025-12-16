package com.br.marketing.dto.datamap;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 节点字典 VO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@Schema(description = "节点字典信息")
public class NodeDictVO {

    @Schema(description = "节点字典ID")
    private Long id;

    @Schema(description = "API代码")
    private String apiCode;

    @Schema(description = "节点代码（类名.方法名）")
    private String nodeCode;

    @Schema(description = "节点类型（API/JOB/RabbitMQ/RocketMQ）")
    private String nodeType;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点描述")
    private String nodeDesc;

    @Schema(description = "是否活跃（1-是 0-否）")
    private Byte isActive;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}


