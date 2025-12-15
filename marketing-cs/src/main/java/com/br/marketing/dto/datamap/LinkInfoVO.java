package com.br.marketing.dto.datamap;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 链路信息 VO
 * 
 * @author Austin
 * @since 2025/10/16
 */
@Data
@Builder
@ApiModel("链路信息")
public class LinkInfoVO {
    
    @ApiModelProperty("链路ID")
    private Long id;

    @ApiModelProperty("apiCode")
    private String apiCode;
    
    @ApiModelProperty("链路代码")
    private String linkCode;

    @ApiModelProperty("链路名称")
    private String linkName;

    @ApiModelProperty("业务场景")
    private String bizScene;

    @ApiModelProperty("链路描述")
    private String description;

    @ApiModelProperty("链路图结构（JSON格式，包含节点、连线、位置等完整信息）")
    private String graphJson;

    @ApiModelProperty("状态（0-禁用 1-启用）")
    private Byte status;

    @ApiModelProperty("总调用次数")
    private Long totalCount;

    @ApiModelProperty("总数据量级")
    private Long totalMagnitude;

    @ApiModelProperty("首次更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime firstUpdateTime;

    @ApiModelProperty("最后更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastUpdateTime;

    @ApiModelProperty("更新次数")
    private Integer updateCount;

    @ApiModelProperty("创建时间")
    private Date createdTime;

    @ApiModelProperty("更新时间")
    private Date updatedTime;
}


