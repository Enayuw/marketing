package com.br.marketing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import org.apache.pulsar.shade.io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 车线索VO
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarClueInfoVo {
    @Schema(description = "id")
    private Long id;
    @Schema(description = "客户编号")
    private String apiCode;
    @Schema(description = "案件编号")
    private String custNum;
    @Schema(description = "上传日期")
    private String appletDate;
    @Schema(description = "线索ID")
    private String clueId;
    @Schema(description = "线索状态")
    private int clueDataStatus;
    @Schema(description = "线索补全状态")
    private int clueCompleteStatus;
    @Schema(description = "外呼意向")
    private String intention;
    @Schema(description = "品牌")
    private String brand;
    @Schema(description = "车系")
    private String series;
    @Schema(description = "城市")
    private String city;
    @Schema(description = "手机号")
    private String cell;
    @ApiModelProperty("清洗时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cleanTime;
    @ApiModelProperty("修改时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @Schema(description = "推送渠道")
    private String cluePushChannel;
    @Schema(description = "推送状态")
    private String cluePushStatus;
    @ApiModelProperty("推送时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pushTime;
    @Schema(description = "入库状态")
    private String clueCallbackFinalState;
    @ApiModelProperty("回调时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date callBackTime;
    @ApiModelProperty("录音地址")
    private String recordingPath;
    @ApiModelProperty("交互文本")
    private String callDiaLog;
    @ApiModelProperty("资源标识")
    private String resourceType;
    @ApiModelProperty("通话记录编号")
    private String callId;
    @ApiModelProperty("错误原因")
    private String clueErrorReason;


}
