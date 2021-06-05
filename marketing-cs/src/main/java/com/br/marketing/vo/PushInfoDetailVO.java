package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class PushInfoDetailVO {

    @ApiModelProperty(value = "任务流水号")
    private Long id;

    @ApiModelProperty(value = "推送时间")
    private Date createTime;

    @ApiModelProperty(value = "客户批次号")
    private String mBatchNumber;

    @ApiModelProperty(value = "模型名称")
    private String mModel;

    @ApiModelProperty(value = "模型版本")
    private String mModelVersion;

    @ApiModelProperty(value = "top最小值")
    private Integer mNumMin;

    @ApiModelProperty(value = "top最大值")
    private Integer mNumMax;

    @ApiModelProperty(value = "最小分值")
    private Integer mScoreMin;

    @ApiModelProperty(value = "最大分值")
    private Integer mScoreMax;

    @ApiModelProperty(value = "推送数量")
    private Integer mRealyNum;

    @ApiModelProperty(value = "执行状态 1-执行中；2-执行成功；3-执行失败")
    private Integer mStatus;

    @ApiModelProperty(value = "执行状态文本描述")
    private String mStatusDesc;

    public String getmStatusDesc(){
        if(mStatus.equals(1)){
            return "执行中";
        }else if(mStatus.equals(2)){
            return "执行成功";
        }else{
            return "执行失败";
        }
    }
}
