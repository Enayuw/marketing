package com.br.marketing.dto.autocheck;

import com.br.marketing.vo.autocheck.TableNameAndFieldVO;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
public class SaveAutoCheckConfigDto {

    @NotBlank(message = "apiCode不能为空")
    private String apiCode;

    @NotBlank(message = "sceneCode不能为空")
    private String sceneCode;

    @NotEmpty(message = "tableNameAndFieldList不能为空")
    private List<TableNameAndFieldVO> tableNameAndFieldList;

    private Boolean isUpdate = false;
}
