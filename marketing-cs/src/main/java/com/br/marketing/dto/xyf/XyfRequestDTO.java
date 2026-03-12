package com.br.marketing.dto.xyf;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import javax.validation.constraints.NotBlank;

@Data
public class XyfRequestDTO {

    @Schema(description = "aesKey")
    @NotBlank(message = "AES密钥必传")
    private String aesKey;

    @Schema(description = "data")
    @NotBlank(message = "data必传")
    private String data;

    @Schema(description = "sign")
    @NotBlank(message = "sign必传")
    private String sign;


    public String validate() {
        if (StringUtils.isEmpty(sign)) {
            return "缺少必输字段sign";
        }
        if (StringUtils.isEmpty(data)) {
            return "缺少必输字段data";
        }
        if (StringUtils.isEmpty(aesKey)) {
            return "缺少必输字段aesKey";
        }
        return null;
    }
}
