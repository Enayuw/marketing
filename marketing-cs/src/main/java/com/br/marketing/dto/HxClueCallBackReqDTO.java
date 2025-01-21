package com.br.marketing.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class HxClueCallBackReqDTO {
    @NotNull(message = "orderId不能为空")
    private String orderId;
    private Integer pushState;
    private Integer finalState;
    private String message;
}
