package com.br.marketing.dto.linkgo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CustomerEncryptDTO {

    @Schema(description = "apicode")
    private String apiCode;

}
