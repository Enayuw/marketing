package com.br.marketing.dto.tccpa;

import com.br.marketing.common.commondto.PageSearchDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaCollidingRuleQueryDTO extends PageSearchDTO {

    @Schema(description = "数据包名称")
    private String packageName;

    @Schema(description = "启用禁用状态")
    private Integer enabled;

    @Schema(description = "撞库日期开始")
    private String collidingDateBegin;

    @Schema(description = "撞库日期结束")
    private String collidingDateEnd;

}
