package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 模板节点详情VO（用于查询返回）
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板节点详情")
public class TemplateNodeDetailVO {

    @Schema(description = "节点ID")
    private Long id;

    @Schema(description = "模板ID")
    private Long templateId;

    @Schema(description = "节点代码（类名.方法名）")
    private String nodeCode;

    @Schema(description = "节点类型（API/JOB/RabbitMQ/RocketMQ）")
    private String nodeType;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;
}
