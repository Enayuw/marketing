package com.br.marketing.vo.datagroup;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class DataGroupConfigVO {


    /**
     *
     */
    @ApiModelProperty(value = "主键id")
    private Long id;

    /**
     * 商户编号
     */
    @ApiModelProperty(value = "apiCode")
    private String apiCode;


    /**
     * 分组规则，json类型
     */
    @ApiModelProperty(value = "分组规则，json类型")
    private String groupRules;

    /**
     * 上传记录统计Id集合
     */
    @ApiModelProperty(value = "上传记录统计Id集合")
    private String uploadReportId;

    /**
     * 1-有效；9-无效
     */
    @ApiModelProperty(value = "1-有效；9-无效")
    private Integer isDel;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private Date updateTime;


}
