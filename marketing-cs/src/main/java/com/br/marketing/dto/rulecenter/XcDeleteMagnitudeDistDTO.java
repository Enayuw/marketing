package com.br.marketing.dto.rulecenter;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class XcDeleteMagnitudeDistDTO {

    @Schema(description = "releaseTime开始时间")
    @NotNull(message = "releaseTime开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeBegin;

    @Schema(description = "releaseTime结束时间")
    @NotNull(message = "releaseTime结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime releaseTimeEnd;

    @Schema(description = "剔除量级")
    private Integer deleteNum;

    @Schema(description = "剩余周期量级")
    private Integer remainingNum;

    @Schema(description = "空挡量级")
    private Integer freeNum;
}
