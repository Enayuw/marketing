package com.br.marketing.dto.sanliuling.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SanLiuLingUploadRequestDTO implements Serializable {
    @Schema(description = "任务id 跟业务一起约定(对应机器人模板号)")
    @JsonProperty("taskId")
    private String taskId;
    @Schema(description = "批次号")
    @JsonProperty("batchNo")
    private String batchNo;
    @Schema(description = "客户列表")
    @JsonProperty("list")
    private List<CustomerInformationDTO> list;
}
