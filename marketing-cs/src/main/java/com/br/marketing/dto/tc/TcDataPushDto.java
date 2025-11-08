package com.br.marketing.dto.tc;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class TcDataPushDto extends TcDataDto{

    @Schema(description = "resetPushFlag")
    private Boolean resetPushFlag;

    @Schema(description = "fileUrl")
    @NotNull(message = "fileUrl必传")
    @NotEmpty(message = "fileUrl必传")
    private String fileUrl;

    @Schema(description = "fileExpirationTime")
    @NotNull(message = "fileExpirationTime必传")
    @NotEmpty(message = "fileExpirationTime必传")
    private String fileExpirationTime;

    @Schema(description = "startDate")
    @NotNull(message = "startDate必传")
    @NotEmpty(message = "startDate必传")
    private String startDate;

    @Schema(description = "endDate")
    @NotNull(message = "endDate必传")
    @NotEmpty(message = "endDate必传")
    private String endDate;

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
        if (StringUtils.isEmpty(startDate)) {
            return "缺少必输字段data.startDate";
        }
        if (StringUtils.isEmpty(endDate)) {
            return "缺少必输字段data.endDate";
        }
        if (null == total) {
            return "缺少必输字段data.total";
        }
        return null;
    }
}
