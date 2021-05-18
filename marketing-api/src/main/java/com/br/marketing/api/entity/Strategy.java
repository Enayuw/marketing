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

/**
 *  公共贷中工具-业务对象-策略定义实体类
 *
 *  @author jilong.xu
 *
 *  2018年3月12日
 */
@TableName("strategy")
@Data
@StategyValidatorAnnotation
public class Strategy extends Model<Strategy> {
    private static final long serialVersionUID = 1L;
    @TableField(value = "id")
    private Integer id;

    @TableField(value = "api_code")
    private String apiCode;

    @TableField(value = "strategy_code")
    private String strCode;

    @NotBlank(message = "策略名称不能为空")
    @Size(max=15, message="策略名称不能超过15个汉字长度")
    @Pattern(regexp="[A-Za-z0-9\\u4e00-\\u9fa5]{1,15}",message = "策略名称仅支持中文、英文、数字组合")
    @TableField(value = "strategy_name")
    private String strName;

    @Size(max=50, message="策略描述不能超过50个汉字长度")
    @TableField(value = "strategy_desc")
    private String strDesc;

    @TableField(value = "use_version")
    private String useVersion;

    @TableField(value = "customer_type")
    private String customerType;

    @TableField(value = "product_type")
    private String prodType;

    @NotBlank(message = "产品名称不能为空")
    @Size(max=15, message="产品名称不能超过15个汉字长度")
    @Pattern(regexp="[0-9A-Za-z\\u4e00-\\u9fa5]{1,15}",message = "产品名称仅支持中文、英文、数字组合")
    @TableField(value = "product_name")
    private String prodName;
    /**
     * //规则集
     * {
     *      "status": 0, // 关
     *      "ruleTypeList":[
     *          {
     *              "ruleType" : "specila",
     *              "version" : "1.0"
     *          }
     *      ]
     *  }
     *
     * [
     *  {
     *      "ruleType" : "specila",
     *      "version" : "1.0",
     *      "productor" : "asd,bvc",
     *      "type" : "hx"
     *  }
     * ]
     *
     * */
    @LeastOneAnnotation
    @TableField(value = "rule_type")
    private String ruleType;

    /** 贷前重审格式说明
     * {
     *     "preLoanStrategy": [
     *         {
     *             "strCode": "STR001",
     *             "strName": "测试策略",
     *             "useVersion":"1.1",
     *             "prodTypeName": "线下现金分期",
     *             "scaneName": "使用場景",
     *             "strDesc": "描述"
     *         },
     *         {
     *              "strCode": "STR001",
     *              "strName": "测试策略",
     *              "useVersion":"1.1",
     *              "prodTypeName": "线下现金分期",
     *              "scaneName": "借款",
     *              "strDesc": "描述"
     *            }
     *     ],
     *     "status": 1 //開
     * }
     */
    @LeastOneAnnotation
    @TableField(value = "strategy_retry")
    private String strategyRetry;

    /**
     *  {
     *  "behaviorScore":[
     *  {
     *  "pro_name":"test",
     *  "pro_code":"test",
     *  "version":"v1.0",
     *  "remark":"測試"
     *  }
     *  ],
     *  "status":1
     *  }
     *
     */
    @LeastOneAnnotation
    @TableField(value = "behavior_score")
    private String behaviorScore;
    @LeastOneAnnotation
    @TableField(value = "selfheal_score")
    private String selfhealScore;
    @TableField(value = "status")
    private Integer status;
    @TableField(value = "can_use")
    private String canUse;
    @TableField(exist = false)
    private Integer ownerId;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }

    @Override
    public String toString() {
        return "Strategy{" +
                "id=" + id +
                ", apiCode='" + apiCode + '\'' +
                ", strCode='" + strCode + '\'' +
                ", strName='" + strName + '\'' +
                ", strDesc='" + strDesc + '\'' +
                ", useVersion='" + useVersion + '\'' +
                ", customerType='" + customerType + '\'' +
                ", prodType='" + prodType + '\'' +
                ", prodName='" + prodName + '\'' +
                ", ruleType='" + ruleType + '\'' +
                ", strategyRetry='" + strategyRetry + '\'' +
                ", behaviorScore='" + behaviorScore + '\'' +
                ", selfhealScore='" + selfhealScore + '\'' +
                ", status=" + status +
                '}';
    }
}
