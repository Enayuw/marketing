package com.br.marketing.dto.autocheck;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class BatchInitAutoCheckTableDictDto {

    @NotEmpty(message = "tableList不能为空")
    @Valid
    private List<AutoCheckTableDictInitDto> tableList;
}

