package com.br.marketing.dto.tc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@Data
public abstract class TcDataDto {

    @Schema(description = "batchNo")
    @NotNull(message = "batchNo必传")
    @NotEmpty(message = "batchNo必传")
    public String batchNo;

    public abstract String validate();
}
