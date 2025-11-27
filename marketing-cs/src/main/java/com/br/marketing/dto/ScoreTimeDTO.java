package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "跑分执行开始时间")
    private String scoreBeginTime;

    @Schema(description = "跑分执行结束时间")
    private String scoreEndTime;



}
