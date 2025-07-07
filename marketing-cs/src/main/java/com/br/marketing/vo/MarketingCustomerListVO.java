package com.br.marketing.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

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
    @Min(1)
    @Max(127)
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

    /**
     * 是否需要校验请求key值（0:不需要,1:需要,2:不需要(通用强校验),3:需要(通用强校验),4:不需要(通用弱校验),5:需要(通用弱校验)）
     */
    @ApiModelProperty(value = "是否需要校验请求key值（0:不需要,1:需要,2:不需要(通用强校验),3:需要(通用强校验),4:不需要(通用弱校验),5:需要(通用弱校验)")
    private Integer isCheck;

    /**
     * 是否计费（不计：0，计费：1）
     */
    @ApiModelProperty(value = "是否计费（不计：0，计费：1）")
    private Integer isCharging;

    /**
     * 请求处理编码
     */
    @ApiModelProperty(value = "请求处理编码")
    private String requestCode;

    /**
     * 响应处理编码
     */
    @ApiModelProperty(value = "响应处理编码")
    private String responseCode;

    /**
     * 账号类型 0 测试 1 正式
     */
    @ApiModelProperty(value = "账号类型 0 测试 1 正式")
    private Integer accountType;

    /**
     * 账号状态：0禁用 1启用)
     */
    @ApiModelProperty(value = "账号状态：0禁用 1启用)")
    private String accountStatus;

    /**
     * 开始时间
     */
    @ApiModelProperty(value = "开始时间")
    private String startTime;

    /**
     * 结束时间
     */
    @ApiModelProperty(value = "结束时间")
    private String endTime;

    /**
     * 0:API 1:WEB 2:SFTP
     */
    @ApiModelProperty(value = "0:API 1:WEB 2:SFTP")
    private Integer transport;

    /**
     *
     */
    private Date officialTime;

    /**
     *
     */
    private String modifyUser;

    /**
     * 加密key
     */
    @ApiModelProperty(value = "加密key")
    private String encryptionKey;

    /**
     * 解密key
     */
    @ApiModelProperty(value = "解密key")
    private String decryptKey;

    /**
     * 流水号：全部默认v2
     */
    @ApiModelProperty(value = "流水号：全部默认v2")
    private String snVer;

    /**
     * 调用方式：1动态监控 2风险扫描与动态监控 3一次性查询 4定期全量监控
     */
    @ApiModelProperty(value = "调用方式：1动态监控 2风险扫描与动态监控 3一次性查询 4定期全量监控")
    private String callMethod;

    /**
     * 文件加密方式 0 不加密 1 流加密 2 压缩加密
     */
    @ApiModelProperty(value = "文件加密方式 0 不加密 1 流加密 2 压缩加密")
    private String fileEncryptionMethods;

    /**
     * 文件加密算法 0 AES-128-CBC 1 AES-256-CBC
     */
    @ApiModelProperty(value = "文件加密算法 0 AES-128-CBC 1 AES-256-CBC")
    private String fileEncryptionAlgorithm;

    /**
     * 文件加密key
     */
    @ApiModelProperty(value = "文件加密key")
    private String fileEncryptionKey;

    /**
     * 创建人
     */
    @ApiModelProperty(value = "创建人")
    private String createUser;

    /**
     * 是否输出数据产品 0 否 1 是
     */
    @ApiModelProperty(value = "是否输出数据产品 0 否 1 是")
    private Integer isOutputDataProduct;

    @ApiModelProperty(value = "校验类型 1-通用校验；2-不校验")
    private Integer checkType;

    /**
     * 跑分分隔符,默认,
     */
    @ApiModelProperty(value = "跑分分隔符")
    private String scoreSeparator;

    /**
     * 客户3k的加密类型 1-MD5;2-sha256
     */
    @ApiModelProperty(value = "客户3k的加密类型:1-MD5;2-sha256")
    private Integer threeKEncryptType;

    /**
     * 短信类别
     */
    @ApiModelProperty(value = "短信类别")
    private String smsCategory;

    /**
     * 一级部门
     */
    @ApiModelProperty(value = "一级部门")
    private String firstDepartment;

    /**
     * 二级部门
     */
    @ApiModelProperty(value = "二级部门")
    private String secondDepartment;

}
