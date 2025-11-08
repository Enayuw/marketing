package com.br.marketing.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class MarketingTransferUserStatusVO {

    @Schema(description = "apicode")
    private String apiCode;

    @Schema(description = "请求批次id")
    private String requestId;

    @Schema(description = "同步状态1-进行中；2-全部成功；3-全部失败；4-部分成功")
    private Integer status;

    @Schema(description = "错误信息")
    private List<MarketingPreUserErrorDetailVO> errorInfo;
}
