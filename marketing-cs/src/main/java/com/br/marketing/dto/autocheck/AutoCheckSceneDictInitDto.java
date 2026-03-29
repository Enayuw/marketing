package com.br.marketing.dto.autocheck;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AutoCheckSceneDictInitDto {

    @NotBlank(message = "sceneCode不能为空")
    private String sceneCode;

    @NotBlank(message = "sceneName不能为空")
    private String sceneName;
}

