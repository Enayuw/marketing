package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;


@Data
public class CarClueReportDTO {
    @NotNull(message = "Page number cannot be null")
    private Integer current;

    @NotNull(message = "Page size cannot be null")
    private Integer size;
    @Schema(description = "品牌、车系、城市")
    private String search;
    @Schema(description = "上传开始时间")
    private String createTimeStart;
    @Schema(description = "上传结束时间")
    private String createTimeEnd;
    @Schema(description = "外呼意向")
    private String intention;
    @Schema(description = "线索来源")
    private String resourceType;
    @Schema(description = "线索状态")
    private List<Integer> clueDataStatus;
    @Schema(description = "线索补全状态")
    private List<Integer> clueCompleteStatus;
    @Schema(description = "修改开始时间")
    private String updateTimeStart;
    @Schema(description = "修改结束时间")
    private String updateTimeEnd;
    @Schema(description = "推送渠道")
    private String cluePushChannel;
    @Schema(description = "推送状态")
    private Integer cluePushStatus;
    @Schema(description = "推送开始时间")
    private String pushTimeStart;
    @Schema(description = "推送结束时间")
    private String pushTimeEnd;
    @Schema(description = "数据入库状态")
    private Integer clueCallbackFinalState;
    @Schema(description = "回调开始时间")
    private String callBackTimeStart;
    @Schema(description = "回调结束时间")
    private String callBackTimeEnd;
    @Schema(description = "排序字段")
    private String orderByField;
    @Schema(description = "排序方式（ASC / DESC）")
    private String orderByType;

}
