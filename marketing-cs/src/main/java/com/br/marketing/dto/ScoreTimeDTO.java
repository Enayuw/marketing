package com.br.marketing.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * -------------------------------
 *
 * @author zhen.Li1
 * @Description
 * @Date 2024/07/18 17:11 PM
 * ------------------------------
 */
@Data
public class ScoreTimeDTO {

    @ApiModelProperty(value = "跑分执行开始时间")
    private String scoreBeginTime;

    @ApiModelProperty(value = "跑分执行结束时间")
    private String scoreEndTime;



}
