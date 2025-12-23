package com.br.marketing.dto.autocheck;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class SaveAutoCheckConfigDto {

    private Long id;

    @NotBlank(message = "apiCode不能为空")
    private String apiCode;

    private String sceneCodes;
}
