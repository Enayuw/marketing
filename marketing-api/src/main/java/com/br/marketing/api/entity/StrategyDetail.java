package com.br.marketing.api.entity;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.br.marketing.api.validator.LeastOneAnnotation;
import com.br.marketing.api.validator.StategyValidatorAnnotation;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;


/**  公共贷中工具-业务对象-策略详情实体类
*
*  @author jilong.xu
*
*  2018年3月12日
*/
@TableName("strategy_detail")
@Data
@StategyValidatorAnnotation
public class StrategyDetail extends Model<StrategyDetail>{
    private static final long serialVersionUID = 1L;

    /**
     * 策略详情id
     */
    @TableField(value = "id")
    private Integer id;

    /**
     * 策略名称
     */
    @NotBlank(message = "策略名称不能为空")
    @Pattern(regexp="[0-9A-Za-z\\u4e00-\\u9fa5]{1,15}",message = "策略名称仅支持中文、英文、数字")
    @Size(max=15, message="策略名称不能超过15个汉字长度")
    @TableField("strategy_name")
    private String strName;

    /**
     * 策略编号
     */
    @TableField("strategy_code")
    private String strCode;

    /**
     * 客户类型
     */
    @TableField(value = "customer_type")
    private String customerType;

    /**
     * 产品类型
     */
    @TableField(value = "product_type")
    private String prodType;

    /**
     * 产品名称
     */
    @NotBlank(message = "产品名称不能为空")
    @Pattern(regexp="[0-9A-Za-z\\u4e00-\\u9fa5]{1,15}",message = "产品名称仅支持中文、英文、数字组合")
    @Size(max=15, message="产品名称不能超过15个汉字长度")
    @TableField(value = "product_name")
    private String prodName;

    /**
     * 商户api_code
     */
    @TableField("api_code")
    private String apiCode;
    /**
     * 策略版本
     */
    @TableField("version")
    private String version;

    /**
     * 策略描述
     */
    @Size(max=50, message="策略描述不能超过50个汉字长度")
    @TableField("strategy_desc")
    private String strDesc;

    /**
     * 规则集
     */
    @LeastOneAnnotation
    @TableField("rule_type")
    private String ruleType;

    /**
     * 贷前策略重审
     */
    @LeastOneAnnotation
    @TableField("strategy_retry")
    private String strategyRetry;

    /**
     * 行为评分
     */
    @LeastOneAnnotation
    @TableField("behavior_score")
    private String behaviorScore;

    /**
     * 自愈评分
     */
    @LeastOneAnnotation
    @TableField("selfheal_score")
    private String selfhealScore;

    /**
     * 状态:1启用2禁用3删除
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建者
     */
    @TableField("create_user")
    private String createUser;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新者
     */
    @TableField("update_user")
    private String updateUser;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;

    /**
     * 是否可用
     */
    @TableField("can_use")
    private String canUse;

    /**
     * 是否开通贷前重审
     */
    @TableField(exist = false)
    private Integer strategyRetryModule;

    /**
     * 是否开通规则集
     */
    @TableField(exist = false)
    private Integer ruleTypeModule;

    /**
     * 是否开通行为评分
     */
    @TableField(exist = false)
    private Integer behaviorScoreModule;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "StrategyDetail{" +
                "id=" + id +
                ", strCode='" + strCode + '\'' +
                ", apiCode='" + apiCode + '\'' +
                ", version='" + version + '\'' +
                ", strDesc='" + strDesc + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", strategyRetry='" + strategyRetry + '\'' +
                ", behaviorScore='" + behaviorScore + '\'' +
                ", selfhealScore='" + selfhealScore + '\'' +
                ", status=" + status +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", updateUser='" + updateUser + '\'' +
                ", updateTime=" + updateTime +
                '}';
    }
}
