package com.br.marketing.client.didi.output.v5;

import lombok.Data;

@Data
public class DiDiV5CollidingResult {

    private Boolean result;
    private Integer failReason;
    private Integer userGroup;
    private Long nextTime;

}
