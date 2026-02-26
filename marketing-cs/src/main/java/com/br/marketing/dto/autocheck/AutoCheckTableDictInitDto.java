package com.br.marketing.dto.autocheck;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AutoCheckTableDictInitDto {

    @NotBlank(message = "tableName不能为空")
    private String tableName;

    @NotBlank(message = "tableDesc不能为空")
    private String tableDesc;
}

