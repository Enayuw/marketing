package com.br.marketing.dto.tccpa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaCollidingRuleDTO {

    @Schema(description = "数据包id集合")
    private Long taskId;

    @Schema(description = "数据包id集合")
    @NotEmpty(message = "数据包id集合不能为空")
    private List<String> packageIds;

    @Schema(description = "剔除规则id集合")
    private List<String> deleteRuleIds;

    @Schema(description = "补包规则releaseTime集合")
    private List<TcyrFailMsgSupplyGroupDTO> failMsgSupplyGroups;

    @Schema(description = "撞库日期集合")
    @NotEmpty(message = "撞库日期集合不能为空")
    private List<String> collidingDates;

    @Schema(description = "撞库时间")
    private String collidingTime;

    @Schema(description = "撞库量级")
    private Integer collidingNum;
}