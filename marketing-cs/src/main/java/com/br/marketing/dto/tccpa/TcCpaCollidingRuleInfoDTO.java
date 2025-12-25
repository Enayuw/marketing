package com.br.marketing.dto.tccpa;

import com.br.marketing.dto.tc.TcyrCpaCollidingDataPackageInfo;
import com.br.marketing.dto.tc.TcyrCpaDeleteRuleInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaCollidingRuleInfoDTO {

    @Schema(description = "数据包")
    private List<TcyrCpaCollidingDataPackageInfo> dataPackages;

    @Schema(description = "剔除规则")
    private List<TcyrCpaDeleteRuleInfo> deleteRules;

    @Schema(description = "提取时间")
    private String extraTime;
}
