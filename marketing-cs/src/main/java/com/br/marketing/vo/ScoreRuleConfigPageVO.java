package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 列表展示跑分配置
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/8/31 14:33
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(description = "跑分配置")
public class ScoreRuleConfigPageVO {

    /**
     * 2021/8/31 16:11 规则主键
     */
    @Schema(description = "规则主键")
    private Long id;

    /**
     * 2021/8/31 16:11 规则与客户关系主键
     */
    @Schema(description = "规则与客户关系主键")
    private Long crId;

    /**
     * 2021/8/31 16:11 规则名称
     */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 2021/8/31 16:11 合作客户ID
     */
    @Schema(description = "合作客户ID")
    private String cid;

    /**
     * 2021/8/31 16:11 接口编码
     */
    @Schema(description = "接口编码")
    private String apiCode;

    /**
     * 2021/8/31 16:11 状态
     */
    @Schema(description = "状态")
    private String status;

    /**
     * 2021/8/31 16:11 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 2021/8/31 16:11 更新时间
     */
    @Schema(description = "更新时间")
    private String updateTime;

    /**
     * 规则简拼
     */
    @Schema(description = "规则简拼")
    private String ruleNameShort;

    @Schema(description = "策略产品配置信息")
    private String strategyProductJson;

    @Schema(description = "返回用户基本字段表头")
    private String baseInfo;

    @Schema(description = "任务执行策略 1-一次性全量；3-每个任务的周期;4-apicode级别统一周期")
    private Integer execType;

    @Schema(description = "周期天数")
    private Integer cycleDay;

    @Schema(description = "周期结束天数")
    private String cycleEndDay;

    @Schema(description = "跑分类型")
    private String taskType;

    @Schema(description = "产品信息")
    private String productInfo;

    @Schema(description = "是否是在线跑分 1-在线；2-离线")
    private Integer isOnline;

    @Schema(description = "跑分优先级 0最高，9最低")
    private Integer priority;

    /**
     * 规则校验状态：0 正常；1 产管不通过暂停自动生成。对应 ScoreRuleCheckStatusEnum。
     */
    @Schema(description = "规则校验状态 0正常 1产管不通过暂停自动生成")
    private Integer checkStatus;

    @Override
    public String toString() {
        return "ScoreRuleConfigPageVO{" +
                "id=" + id +
                ", crId=" + crId +
                ", ruleName='" + ruleName + '\'' +
                ", cid='" + cid + '\'' +
                ", apiCode='" + apiCode + '\'' +
                ", status='" + status + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", ruleNameShort='" + ruleNameShort + '\'' +
                ", strategyProductJson='" + strategyProductJson + '\'' +
                ", baseInfo='" + baseInfo + '\'' +
                ", execType=" + execType +
                ", cycleDay=" + cycleDay +
                ", cycleEndDay='" + cycleEndDay + '\'' +
                ", taskType='" + taskType + '\'' +
                ", productInfo='" + productInfo + '\'' +
                ", isOnline=" + isOnline +
                ", priority=" + priority +
                ", checkStatus=" + checkStatus +
                '}';
    }
}
