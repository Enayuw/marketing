package com.br.marketing.dto.tc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class TcFailDataPushDto extends TcDataDto{

    @Schema(description = "fileUrl")
    @NotNull(message = "fileUrl必传")
    @NotEmpty(message = "fileUrl必传")
    private String fileUrl;

    @Schema(description = "fileExpirationTime")
    @NotNull(message = "fileExpirationTime必传")
    @NotEmpty(message = "fileExpirationTime必传")
    private String fileExpirationTime;

    @Schema(description = "total")
    @NotNull(message = "total必传")
    @NotEmpty(message = "total必传")
    private Long total;

    public String validate() {
        if (StringUtils.isEmpty(batchNo)) {
            return "缺少必输字段data.batchNo";
        }
        if (StringUtils.isEmpty(fileUrl)) {
            return "缺少必输字段data.fileUrl";
        }
        if (StringUtils.isEmpty(fileExpirationTime)) {
            return "缺少必输字段data.fileExpirationTime";
        }
        if (null == total) {
            return "缺少必输字段data.total";
        }
        return null;
    }
}
