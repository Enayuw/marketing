package com.br.marketing.dto.tc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class TcRequestDTO {

    @Schema(description = "requestNo")
    @NotNull(message = "requestNo必传")
    @NotEmpty(message = "requestNo必传")
    private String requestNo;

    @Schema(description = "sign")
    @NotNull(message = "sign必传")
    @NotEmpty(message = "sign必传")
    private String sign;

    @Schema(description = "timestamp")
    @NotNull(message = "timestamp必传")
    @NotEmpty(message = "timestamp必传")
    private String timestamp;

    @Schema(description = "data")
    @NotNull(message = "data必传")
    @NotEmpty(message = "data必传")
    private String data;

    public String validate() {
        if (StringUtils.isEmpty(requestNo)) {
            return "缺少必输字段requestNo";
        }
        if (StringUtils.isEmpty(sign)) {
            return "缺少必输字段sign";
        }
        if (StringUtils.isEmpty(timestamp)) {
            return "缺少必输字段timestamp";
        }
        if (StringUtils.isEmpty(data)) {
            return "缺少必输字段data";
        }
        return null;
    }

}
