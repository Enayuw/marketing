package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板节点VO
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板节点信息")
public class TemplateNodeVO {

    @Schema(description = "临时节点ID（前端生成，用于边引用节点，如：node_1, node_2）")
    private String tempId;

    @Schema(description = "节点代码（类名.方法名）")
    private String nodeCode;

    @Schema(description = "节点类型（API/JOB/RabbitMQ/RocketMQ）")
    private String nodeType;

    @Schema(description = "节点名称")
    private String nodeName;
}
