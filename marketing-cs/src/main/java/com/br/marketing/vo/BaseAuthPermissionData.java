package com.br.marketing.vo;

import java.io.Serializable;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class BaseAuthPermissionData implements Serializable {
    private static final long serialVersionUID = 8924914030959991453L;
    @Schema(description = "apiCode集合")
    private List<String> apiCodes;
}
