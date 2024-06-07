package com.br.marketing.vo.dataclean;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DataCleanTaskVO {


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
     * 文件类型  0:上传文件 1:转化文件
     */
    @ApiModelProperty(value = "文件类型  0:上传文件 1:转化文件")
    private Integer fileType;


    /**
     * 文件id
     */
    @ApiModelProperty(value = "文件id")
    private String fileId;

    /**
     * 文件名称
     */
    @ApiModelProperty(value = "文件名称")
    private String fileName;

    /**
     * 规则配置id
     */
    @ApiModelProperty(value = "规则ID")
    private Integer configId;

    /**
     * 规则配置id
     */
    @ApiModelProperty(value = "规则名称")
    private String configName;

    /**
     * 任务状态
     */
    @ApiModelProperty(value = "任务状态")
    private Integer cleanStatus;

    /**
     * 试跑结果
     */
    @ApiModelProperty(value = "试跑结果")
    private String testResult;

    /**
     * 创建时间
     */
    @ApiModelProperty(value = "创建时间")
    private String createTime;

    /**
     * 修改时间
     */
    @ApiModelProperty(value = "修改时间")
    private String updateTime;

    /**
     * 组装对象
     */
    @ApiModelProperty(value = " 组装对象")
    private String ruleCondition;


}
