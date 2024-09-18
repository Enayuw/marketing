package com.br.marketing.vo.zhongan.param;

import io.swagger.annotations.ApiModelProperty;
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

    @ApiModelProperty(value = "数据日期")
    private String reportDate;
    @ApiModelProperty(value = "场景1")
    private List<ZhongAnCustomInfo> userType1;
    @ApiModelProperty(value = "场景7")
    private List<ZhongAnCustomInfo> userType7;
    @ApiModelProperty(value = "场景8")
    private List<ZhongAnCustomInfo> userType8;

}
