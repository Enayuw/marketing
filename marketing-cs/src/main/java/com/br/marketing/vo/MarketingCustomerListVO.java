package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 用户信息列表返回
 *
 * @author songjuanjuan
 * @dateTime 2021/10/21 17:49
 */
@Data
@ApiModel(value = "客户表")
public class MarketingCustomerListVO {

    /**
     * 主键id
     */
    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * api_code
     */
    @ApiModelProperty(value = "api_code")
    @NotEmpty
    private String apiCode;

    /**
     * 合作客户ID
     */
    @ApiModelProperty(value = "合作客户ID")
    @NotEmpty
    private String cid;

    /**
     * 合作客户名称
     */
    @ApiModelProperty(value = "合作客户名称")
    @NotEmpty
    private String name;

    /**
     * 合作客户简称
     */
    @ApiModelProperty(value = "合作客户简称")
    @NotEmpty
    private String shortName;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String message;

    /**
     * 并发数
     */
    @ApiModelProperty(value = "线程数")
    @Max(value = 100)
    @Min(value = 1)
    private Integer threadNum;

    /**
     * 跑分顺序根据此字段倒序排序
     */
    @ApiModelProperty(value = "跑分顺序")
    private Byte sort;

    /**
     * 状态 1正常，0删除
     */
    @ApiModelProperty(value = "状态(1正常;0删除)")
    private Byte status;

    /**
     * 扩展字段
     */
    @ApiModelProperty(value = "扩展字段")
    private String extendConfigInfo;

    /**
     * api推送并发数
     */
    @ApiModelProperty(value = "api推送并发数")
    private Integer pushThreadNum;

    /**
     * 跑分结果推送类型，0文件，1 api，默认支持文件推送
     */
    @ApiModelProperty(value = "跑分结果推送类型(0文件;1 api,默认支持文件推送)")
    private Integer pushType;

    /**
     * 推送地址
     */
    @ApiModelProperty(value = "推送地址")
    private String pushUrl;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty(value = "更新时间")
    private String updateTime;

}
