package com.br.marketing.vo.zhongan.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName ZhongAnCustomInfoVO
 * @Author kongbx
 * @Date 2024/9/18 16:18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhongAnCustomInfo {

    @Schema(description = "组别")
    private Integer constituencies;
    @Schema(description = "总数据量")
    private Integer totalNum;
    @Schema(description = "进件人数")
    private Integer incomingNum;
    @Schema(description = "批核人数")
    private Integer approversNum;
    @Schema(description = "登录率")
    private String loginRate;
    @Schema(description = "批核件均")
    private Integer approvalAvailable;
    @Schema(description = "发起提现人数")
    private Integer applyPayNum;
    @Schema(description = "提现通过通过率")
    private String payPassRate;
    @Schema(description = "放款成功人数")
    private Integer lendersSucNum;
    @Schema(description = "放款成功金额")
    private String lendersSucAmount;
}
