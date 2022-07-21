package com.br.marketing.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
public class ResultPreviewVO {
    @ApiModelProperty(value = "表头信息",notes = "name-表头名称；status-表头状态0（无数据）、1（有数据）； 样例:[{\"name\":\"custNum\",\"status\":0},{\"name\":\"cell\",\"status\":1}]")
    private List<HashMap> headDesc;
    @ApiModelProperty(value = "数据信息",notes = "样例[{\"custNum\":\"123\",\"cell\":\"456\"},{\"custNum\":\"111\",\"cell\":\"222\"}")
    private List<HashMap> content;
}
