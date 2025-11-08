package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
public class RequestCommonDTO<T> {

    @Schema(description = "apicode")
    @NotNull(message = "apiCode必传")
    @NotEmpty(message = "apiCode必传")
    private String apiCode;

    @Schema(description = "传输的json数据")
    @NotNull(message = "jsonData必传")
    private T jsonData;
}
