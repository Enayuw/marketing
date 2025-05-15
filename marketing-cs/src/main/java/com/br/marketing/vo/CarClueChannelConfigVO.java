package com.br.marketing.vo;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @ClassName CarClueChannelConfigVO
 * @Author kongbx
 * @Date 2025/5/15 11:07
 */
@Data
public class CarClueChannelConfigVO {

    @ApiModelProperty(value = "客户id(客户表中的主键id)")
    private Long id;

    @ApiModelProperty(value = "易车KA拉取时间")
    private String pullDate;

    @ApiModelProperty(value = "外呼意向等级配置")
    private JSONObject intentionConfig;

    @ApiModelProperty(value = "数据清洗类型 0-手动执行 1-自动执行")
    private Integer cleanType;

    @ApiModelProperty(value = "数据推送类型 0-手动执行 1-自动执行")
    private Integer pullType;

    @ApiModelProperty(value = "操作人id")
    private Long optUserId;

    @ApiModelProperty(value = "操作人账户名")
    private String optUserName;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "")
    private Date updateTime;

    @ApiModelProperty(value = "1-有效；9-无效")
    private Integer isDel;


}
