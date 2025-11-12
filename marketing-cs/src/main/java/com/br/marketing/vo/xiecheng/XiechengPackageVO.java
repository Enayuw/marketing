package com.br.marketing.vo.xiecheng;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "携程撞库数据包VO")
public class XiechengPackageVO implements Serializable {

    private static final long serialVersionUID = 9027984842563179835L;
    @Schema(description = "主键id")
    private Long id;

    @Schema(description = "数据包名称")
    private String packageName;

    @Schema(description = "撞库数据清洗任务id")
    private Long collidingDataTaskId;

    @Schema(description = "优先级")
    private Integer priority;

    @Schema(description = "ApiCode")
    private Integer apiCode;

}
