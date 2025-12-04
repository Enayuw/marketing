package com.br.marketing.dto.tccpa;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaCollidingRuleDTO {

    @ApiModelProperty(value = "数据包id集合")
    @NotEmpty(message = "数据包id集合不能为空")
    private List<String> packageIds;

    @ApiModelProperty(value = "剔除规则id集合")
    private List<String> deleteRuleIds;

    @ApiModelProperty(value = "补包规则releaseTime集合")
    private List<TcyrFailMsgSupplyGroupDTO> failMsgSupplyGroups;

    @ApiModelProperty(value = "提取量级阈值")
    private Integer limitNum;

    @ApiModelProperty(value = "撞库日期集合")
    @NotEmpty(message = "撞库日期集合不能为空")
    private List<String> collidingDates;

    @ApiModelProperty(value = "撞库时间")
    private String collidingTime;
}
