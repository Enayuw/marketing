package com.br.marketing.dto.tccpa;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaCollidingTaskDTO {

    @ApiModelProperty(value = "撞库任务id")
    private Long id;

    @ApiModelProperty(value = "客户编号")
    private String cid;

    @ApiModelProperty(value = "客户名称")
    private String customerName;

    @ApiModelProperty(value = "商户编号")
    private String apiCode;

    @ApiModelProperty(value = "数据包id集合")
    private List<String> packageIds;

    @ApiModelProperty(value = "数据包名称集合")
    private String packageNames;

    @ApiModelProperty(value = "撞库日期")
    private String collidingDate;

    @ApiModelProperty(value = "撞库时间")
    private String collidingTime;

    @ApiModelProperty(value = "撞库量级上限")
    private Integer limitNum;

    @ApiModelProperty(value = "剔除规则id集合")
    private List<String> deleteRuleIds;

    @ApiModelProperty(value = "剔除量级")
    private Integer deleteNum;

    @ApiModelProperty(value = "补包releaseTime")
    private String releaseTimes;

    @ApiModelProperty(value = "补包量级")
    private Integer supplyNum;

    @ApiModelProperty(value = "推送时间")
    private Date pushTime;

    @ApiModelProperty(value = "推送状态")
    private Integer status;

    @ApiModelProperty(value = "禁用状态")
    private Integer enabled;

    private Integer isDel;

    private Date createTime;

    private Date updateTime;

}
