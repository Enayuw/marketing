package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 跑分配置VO
 *
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/9/2 11:06
 */
@Data
@Schema(description = "跑分配置")
public class ScoreRuleVO implements Serializable {

    private static final long serialVersionUID = 733176891423773795L;
    /**
     * 2021/8/31 16:11 规则主键
     */
    @Schema(description = "规则主键")
    private Long id;

    /**
     * 2021/8/31 16:11 规则名称
     */
    @Schema(description = "规则名称")
    @NotEmpty(message = "规则名称不可为空")
    @Length(min = 1, max = 60, message = "规则名称长度不合法")
    private String ruleName;

    /**
     * 2021/8/31 16:11 合作客户ID
     */
    @Schema(description = "合作客户ID")
    @NotEmpty(message = "合作客户ID不可为空")
    private String cid;

    /**
     * 2021/8/31 16:11 接口编码
     */
    @Schema(description = "接口编码")
    @NotEmpty(message = "接口编码不可为空")
    private String apiCode;


    @Schema(description = "场景变量列表")
    @NotNull(message = "场景不可为空")
    private List<Map<String, Object>> variableList;

    /**
     * 跑分时间 格式HH:mm
     */
    @Schema(description = "跑分时间 格式HH:mm")
    @NotEmpty(message = "跑分时间不可为空")
    @Pattern(regexp = "^([0-1]?[0-9]|2[0-3]):([0-5][0-9])$", message = "诶呦喂！时间格式不正确(格式HH:mm)")
    private String startTime;

    /**
     * 策略产品展示信息，后期有可能维护成需要配置的产品
     */
    @Schema(description = "策略产品展示信息，后期有可能维护成需要配置的产品")
    private String strategyProductShow;

    /**
     * 策略
     */
    @Schema(description = "策略主键")
    private String strategyId;

    /**
     * 规则简拼
     */
    @Schema(description = "规则简拼")
    private String ruleNameShort;

    @Schema(description = "策略产品配置信息")
    private String strategyProductJson;

    @Schema(description = "返回用户基本字段表头")
    private String baseInfo;

    @Schema(description = "任务执行策略 1-一次性全量；3-每个任务的周期;4-每日定时")
    private Integer execType;

    @Schema(description = "周期天数")
    private Integer cycleDay;

    @Schema(description = "周期结束天数")
    private String cycleEndDay;

    @Schema(description = "跑分类型 如果不跑分0-策略跑分；1-数据透析；2-产品跑分")
    private Integer taskType;

    @Schema(description = "产品信息")
    private String productInfo;

    @Schema(description = "3k值加密方式 0-不加密；1-md5；2-sha256")
    private Integer threekEncryptType;

    @Schema(description = "是否是在线跑分 1-在线；2-离线")
    private Integer isOnline;

    @Schema(description = "是否叠加有效期数据 0-否，1-是")
    private Integer isStackValidity;

    @Schema(description = "跑分优先级 0~9，0最高，9最小，默认值9")
    private Integer priority;

    public ScoreRuleVO() {
    }

    public ScoreRuleVO(Long id, String ruleName, String cid, String apiCode, List<Map<String, Object>> variableList
            , String startTime, String strategyProductShow, String strategyId) {
        this.id = id;
        this.ruleName = ruleName;
        this.cid = cid;
        this.apiCode = apiCode;
        this.variableList = variableList;
        this.startTime = startTime;
        this.strategyProductShow = strategyProductShow;
        this.strategyId = strategyId;
    }
}
