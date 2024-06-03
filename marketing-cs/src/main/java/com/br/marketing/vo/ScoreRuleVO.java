package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Set;

/**
 * 跑分配置VO
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/2 11:06
 */
@Data
@ApiModel(value = "跑分配置")
public class ScoreRuleVO implements Serializable {

    private static final long serialVersionUID = 733176891423773795L;
    /**
     * 2021/8/31 16:11 规则主键
     */
    @ApiModelProperty(value = "规则主键", dataType = "long", position = 1)
    private Long id;

    /**
     * 2021/8/31 16:11 规则名称
     */
    @ApiModelProperty(value = "规则名称", dataType = "string", position = 2)
    @NotEmpty(message = "规则名称不可为空")
    @Length(min = 1, max = 60, message = "规则名称长度不合法")
    private String ruleName;

    /**
     * 2021/8/31 16:11 合作客户ID
     */
    @ApiModelProperty(value = "合作客户ID", dataType = "string", position = 3)
    @NotEmpty(message = "合作客户ID不可为空")
    private String cid;

    /**
     * 2021/8/31 16:11 接口编码
     */
    @ApiModelProperty(value = "接口编码", dataType = "string", position = 4)
    @NotEmpty(message = "接口编码不可为空")
    private String apiCode;

    @ApiModelProperty(value = "客户配置变量值字典集合", dataType = "array", position = 5)
    @NotNull(message = "场景不可为空")
    private Set<VariableDicSelectVO> vdSet;

    /**
     * 跑分时间 格式HH:mm
     */
    @ApiModelProperty(value = "跑分时间 格式HH:mm", dataType = "string", position = 8)
    @NotEmpty(message = "跑分时间不可为空")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):([0-5][0-9])$", message = "诶呦喂！时间格式不正确(格式HH:mm)")
    private String startTime;

    /**
     * 策略产品展示信息，后期有可能维护成需要配置的产品
     */
    @ApiModelProperty(value = "策略产品展示信息，后期有可能维护成需要配置的产品", dataType = "string", position = 7)
    private String strategyProductShow;

    /**
     * 策略
     */
    @ApiModelProperty(value = "策略主键", dataType = "string", position = 6)
    private String strategyId;

    /**
     * 规则简拼
     */
    @ApiModelProperty(value = "规则简拼", dataType = "string", position = 7)
    private String ruleNameShort;

    @ApiModelProperty(value = "策略产品配置信息", dataType = "string", position = 8)
    private String strategyProductJson;

    @ApiModelProperty(value = "返回用户基本字段表头", dataType = "string", position = 9)
    private String baseInfo;

    @ApiModelProperty(value = "任务执行策略 1-一次性全量；3-每个任务的周期;4-每日定时", dataType = "integer", position = 10)
    private Integer execType;

    @ApiModelProperty(value = "周期天数", dataType = "integer", position = 11)
    private Integer cycleDay;

    @ApiModelProperty(value = "周期结束天数", dataType = "string", position = 12)
    private String cycleEndDay;

    @ApiModelProperty(value = "跑分类型 如果不跑分0-策略跑分；1-数据透析；2-产品跑分", dataType = "integer", position = 13)
    private Integer taskType;

    @ApiModelProperty(value = "产品信息", dataType = "string", position = 14)
    private String productInfo;

    @ApiModelProperty(value = "3k值加密方式 0-不加密；1-md5；2-sha256", dataType = "string", position = 15)
    private Integer threekEncryptType;

    @ApiModelProperty(value = "是否是在线跑分 1-在线；2-离线", dataType = "string", position = 15)
    private Integer isOnline;

    @ApiModelProperty(value = "是否叠加有效期数据 0-否，1-是", dataType = "integer", position = 11)
    private Integer isStackValidity;

    public ScoreRuleVO() {
    }

    public ScoreRuleVO(Long id, String ruleName, String cid, String apiCode, Set<VariableDicSelectVO> vdSet
            , String startTime, String strategyProductShow, String strategyId) {
        this.id = id;
        this.ruleName = ruleName;
        this.cid = cid;
        this.apiCode = apiCode;
        this.vdSet = vdSet;
        this.startTime = startTime;
        this.strategyProductShow = strategyProductShow;
        this.strategyId = strategyId;
    }
}
