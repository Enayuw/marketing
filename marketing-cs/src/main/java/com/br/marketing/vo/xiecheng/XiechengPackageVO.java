package com.br.marketing.vo.xiecheng;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "携程撞库数据包VO")
public class XiechengPackageVO implements Serializable {

    private static final long serialVersionUID = 9027984842563179835L;
    @ApiModelProperty("主键id")
    private Long id;

    @ApiModelProperty("数据包名称")
    private String packageName;

    @ApiModelProperty("撞库数据清洗任务id")
    private Long collidingDataTaskId;

    @ApiModelProperty("优先级")
    private Integer priority;

}
