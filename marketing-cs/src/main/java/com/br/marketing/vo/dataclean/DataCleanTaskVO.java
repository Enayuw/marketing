package com.br.marketing.vo.dataclean;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DataCleanTaskVO {


    /**
     *
     */
    @Schema(description = "主键id")
    private Long id;

    /**
     * 商户编号
     */
    @Schema(description = "apiCode")
    private String apiCode;


    /**
     * 文件类型  0:上传文件 1:转化文件
     */
    @Schema(description = "文件类型  0:上传文件 1:转化文件")
    private Integer fileType;


    /**
     * 文件id
     */
    @Schema(description = "文件id")
    private String fileId;

    /**
     * 文件名称
     */
    @Schema(description = "文件名称")
    private String fileName;

    /**
     * 规则配置id
     */
    @Schema(description = "规则ID")
    private Integer configId;

    /**
     * 规则配置id
     */
    @Schema(description = "规则名称")
    private String ruleName;

    /**
     * 任务状态
     */
    @Schema(description = "任务状态")
    private Integer cleanStatus;

    /**
     * 试跑结果
     */
    @Schema(description = "试跑结果")
    private String testResult;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private String createTime;

    /**
     * 修改时间
     */
    @Schema(description = "修改时间")
    private String updateTime;

    /**
     * 组装对象
     */
    @Schema(description = " 组装对象")
    private String ruleCondition;


}
