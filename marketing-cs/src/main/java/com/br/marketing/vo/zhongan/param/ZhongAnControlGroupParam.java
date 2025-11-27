package com.br.marketing.vo.zhongan.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @ClassName ZhongAnControlGroupVO
 * @Description 众安对照组配置
 * @Author kongbx
 * @Date 2024/9/18 15:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ZhongAnControlGroupParam implements Serializable {

    @Schema(description = "数据日期")
    private String reportDate;
    @Schema(description = "场景1")
    private List<ZhongAnCustomInfo> userType1;
    @Schema(description = "场景7")
    private List<ZhongAnCustomInfo> userType7;
    @Schema(description = "场景8")
    private List<ZhongAnCustomInfo> userType8;

}
