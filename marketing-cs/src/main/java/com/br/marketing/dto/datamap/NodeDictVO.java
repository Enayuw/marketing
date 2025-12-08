package com.br.marketing.dto.datamap;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 节点字典 VO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@ApiModel("节点字典信息")
public class NodeDictVO {

    @ApiModelProperty("节点字典ID")
    private Long id;

    @ApiModelProperty("API代码")
    private String apiCode;

    @ApiModelProperty("节点代码（类名.方法名）")
    private String nodeCode;

    @ApiModelProperty("节点类型（API/JOB/RabbitMQ/RocketMQ）")
    private String nodeType;

    @ApiModelProperty("节点名称")
    private String nodeName;

    @ApiModelProperty("节点描述")
    private String nodeDesc;

    @ApiModelProperty("是否活跃（1-是 0-否）")
    private Byte isActive;

    @ApiModelProperty("创建时间")
    private Date createTime;

    @ApiModelProperty("更新时间")
    private Date updateTime;
}


