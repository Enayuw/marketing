package com.br.marketing.dto.datamap.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板节点字典VO（去重后的节点信息，用于模板配置时选择）
 *
 * @author bingxu.kong
 * @since 2025/01/27
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "模板节点字典信息")
public class TemplateNodeDictVO {

    @Schema(description = "节点代码（类名.方法名）")
    private String nodeCode;

    @Schema(description = "节点类型（API/JOB/RabbitMQ/RocketMQ）")
    private String nodeType;

    @Schema(description = "节点名称")
    private String nodeName;

    @Schema(description = "节点描述")
    private String nodeDesc;
}
