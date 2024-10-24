package com.br.marketing.entity;

import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * b_report_task
 * @author 
 */
@Data
public class ReportTask implements Serializable {
    private Long id;

    /**
     * 报表名称
     */
    @ApiModelProperty(value="报表名称")
    private String reportName;

    /**
     * 报表规则集
     */
    @ApiModelProperty(value="报表规则集")
    private String reportRules;

    /**
     * 文件下载地址
     */
    @ApiModelProperty(value="文件下载地址")
    private String downloadUrl;

    /**
     * 状态 0-待开始；1-统计中；2-已完成；3-统计失败
     */
    @ApiModelProperty(value="状态 0-待开始；1-统计中；2-已完成；3-统计失败")
    private Integer status;

    /**
     * 下载状态 0-未生成；1-文件生成中；2-文件已生成；
     */
    @ApiModelProperty(value="下载状态 0-未生成；1-文件生成中；2-文件已生成；")
    private Integer downStatus;

    /**
     * 报表类型1-跑分模型分布;2-携程月转化报表;3-携程日转化报表；4-单日撞库结果分布；数据使用率
     */
    @ApiModelProperty(value="报表类型1-跑分模型分布;2-携程月转化报表;3-携程日转化报表；4-单日撞库结果分布；数据使用率")
    private Integer reportType;

    /**
     * 产品分为x、y之后组合的数量
     */
    @ApiModelProperty(value="产品分为x、y之后组合的数量")
    private Integer groupCount;

    /**
     * 1-有效；9-无效
     */
    @ApiModelProperty(value="1-有效；9-无效")
    private Integer isDel;

    /**
     * 创建时间
     */
    @ApiModelProperty(value="创建时间")
    private Date createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value="修改时间")
    private Date updateTime;

    /**
     * 统计类型1-手动统计；2-自动统计
     */
    @ApiModelProperty(value="统计类型1-手动统计；2-自动统计")
    private Integer statisticsType;

    /**
     * 统计时间
     */
    @ApiModelProperty(value="统计时间")
    private String statisticsTime;

    private static final long serialVersionUID = 1L;
}