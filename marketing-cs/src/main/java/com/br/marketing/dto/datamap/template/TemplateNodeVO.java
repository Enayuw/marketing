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

    @Schema(description = "节点ID（主键）")
    private Long id;

    @Schema(description = "节点字典ID")
    private Long nodeDictId;

    @Schema(description = "节点名称")
    private String nodeName;
}
