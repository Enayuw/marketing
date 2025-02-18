package com.br.marketing.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "客户编号")
    private String apiCode;
    @ApiModelProperty(value = "案件编号")
    private String custNum;
    @ApiModelProperty(value = "上传日期")
    private String appletDate;
    @ApiModelProperty(value = "线索ID")
    private String clueId;
    @ApiModelProperty(value = "线索状态")
    private int clueDataStatus;
    @ApiModelProperty(value = "线索补全状态")
    private int clueCompleteStatus;
    @ApiModelProperty(value = "外呼意向")
    private String intention;
    @ApiModelProperty(value = "品牌")
    private String brand;
    @ApiModelProperty(value = "车系")
    private String series;
    @ApiModelProperty(value = "城市")
    private String city;
    @ApiModelProperty(value = "手机号")
    private String cell;
    @ApiModelProperty("清洗时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date cleanTime;
    @ApiModelProperty("修改时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    @ApiModelProperty(value = "推送渠道")
    private String cluePushChannel;
    @ApiModelProperty(value = "推送状态")
    private String cluePushStatus;
    @ApiModelProperty("推送时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date pushTime;
    @ApiModelProperty(value = "入库状态")
    private String clueCallbackFinalState;
    @ApiModelProperty("回调时间 yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date callBackTime;
    @ApiModelProperty("录音地址")
    private String recordingPath;
    @ApiModelProperty("资源标识")
    private String resourceType;

}
