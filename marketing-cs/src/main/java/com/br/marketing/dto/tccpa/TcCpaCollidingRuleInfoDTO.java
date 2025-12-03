package com.br.marketing.dto.tccpa;

import com.br.marketing.dto.tc.TcyrCpaCollidingDataPackageInfo;
import com.br.marketing.dto.tc.TcyrCpaDeleteRuleInfo;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaCollidingRuleInfoDTO {

    @ApiModelProperty(value = "数据包")
    private List<TcyrCpaCollidingDataPackageInfo> dataPackages;

    @ApiModelProperty(value = "剔除规则")
    private List<TcyrCpaDeleteRuleInfo> deleteRules;

    @ApiModelProperty(value = "提取量级阈值")
    private Integer extraNumTotal;

    @ApiModelProperty(value = "提取时间")
    private String extraTime;
}
