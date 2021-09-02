package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
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
    private String startTime;

    /**
     * 策略产品配置信息
     */
    @ApiModelProperty(value = "策略产品配置信息", dataType = "string", position = 7)
    private String strategyProductJson;

    /**
     * 策略
     */
    @ApiModelProperty(value = "策略主键", dataType = "string", position = 6)
    private String strategyId;

    public ScoreRuleVO() {
    }

    public ScoreRuleVO(Long id, String ruleName, String cid, String apiCode, Set<VariableDicSelectVO> vdSet
            , String startTime, String strategyProductJson, String strategyId) {
        this.id = id;
        this.ruleName = ruleName;
        this.cid = cid;
        this.apiCode = apiCode;
        this.vdSet = vdSet;
        this.startTime = startTime;
        this.strategyProductJson = strategyProductJson;
        this.strategyId = strategyId;
    }
}
