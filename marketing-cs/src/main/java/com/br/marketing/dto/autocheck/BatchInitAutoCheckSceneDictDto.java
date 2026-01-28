package com.br.marketing.dto.autocheck;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchInitAutoCheckSceneDictDto {

    @NotEmpty(message = "sceneList不能为空")
    @Valid
    private List<AutoCheckSceneDictInitDto> sceneList;
}

