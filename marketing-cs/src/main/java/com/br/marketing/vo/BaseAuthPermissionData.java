package com.br.marketing.vo;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class BaseAuthPermissionData implements Serializable {
    private static final long serialVersionUID = 8924914030959991453L;
    @ApiModelProperty(value = "apiCode集合")
    private List<String> apiCodes;
}
