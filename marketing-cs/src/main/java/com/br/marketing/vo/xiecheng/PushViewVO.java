package com.br.marketing.vo.xiecheng;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "规则中心-预览数据接口")
public class PushViewVO {

    @Schema(description = "筛选结果")
    private String result;

    @Schema(description = "筛选数量")
    private Integer total;

    @Schema(description = "重推框定数据时间")
    private String repushTime;

}
