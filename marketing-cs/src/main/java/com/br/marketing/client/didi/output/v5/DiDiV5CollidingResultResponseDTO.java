package com.br.marketing.client.didi.output.v5;

import lombok.Data;

@Data
public class DiDiV5CollidingResultResponseDTO {
    private String errorCode;
    private String errorMessage;
    private DiDiV5CollidingResult data;
}
