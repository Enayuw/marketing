package com.br.marketing.vo.xiecheng;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "规则中心携程撞库数据")
public class XiechengCollidingDataVO {


    @Schema(description = "ApiCode")
    private String apiCode;

    @Schema(description = "撞库结果数据")
    private String resultData;


    @Schema(description = "更新时间")
    private String updateTime;

    @Schema(description = "数据包数据量")
    private String resultNum;

}
