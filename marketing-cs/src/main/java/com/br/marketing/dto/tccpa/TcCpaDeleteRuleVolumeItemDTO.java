package com.br.marketing.dto.tccpa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条剔除规则在数据包维度下的剔除量级。
 * 仅启用该规则时：全量 − 应用规则后的量级。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaDeleteRuleVolumeItemDTO {

    @Schema(description = "剔除规则 id")
    private Long ruleId;

    @Schema(description = "剔除规则名称")
    private String ruleName;

    @Schema(description = "该规则单独作用时的剔除量级")
    private Integer deleteNum;
}
