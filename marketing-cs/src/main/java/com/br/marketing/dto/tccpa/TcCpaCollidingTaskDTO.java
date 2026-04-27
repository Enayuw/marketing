package com.br.marketing.dto.tccpa;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TcCpaCollidingTaskDTO {

    @Schema(description = "撞库任务id")
    private Long id;

    @Schema(description = "客户编号")
    private String cid;

    @Schema(description = "客户名称")
    private String customerName;

    @Schema(description = "商户编号")
    private String apiCode;

    @Schema(description = "数据包id集合")
    private List<String> packageIds;

    @Schema(description = "数据包名称集合")
    private String packageNames;

    @Schema(description = "撞库日期")
    private String collidingDate;

    @Schema(description = "撞库时间")
    private String collidingTime;

    @Schema(description = "撞库量级")
    private Integer collidingNum;

    @Schema(description = "剔除规则id集合")
    private List<String> deleteRuleIds;

    @Schema(description = "剔除量级")
    private Integer deleteNum;

    @Schema(description = "剔除详情")
    private String deleteInfo;

    @Schema(description = "补包releaseTime")
    private String releaseTimes;

    @Schema(description = "补包量级")
    private Integer supplyNum;

    @Schema(description = "预估量级")
    private Integer estNum;

    @Schema(description = "推送量级")
    private Integer pushNum;

    @Schema(description = "推送状态")
    private Integer status;

    @Schema(description = "禁用状态")
    private Integer enabled;

    private Integer isDel;

    private Date createTime;

    private Date updateTime;

}