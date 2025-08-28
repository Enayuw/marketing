package com.br.marketing.dto.sanliuling.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SanLiuLingUploadRequestDTO implements Serializable {
    private static final long serialVersionUID = -1L;

    @ApiModelProperty("任务id 跟业务一起约定(对应机器人模板号)")
    @JsonProperty("taskId")
    private String taskId;
    @ApiModelProperty("批次号")
    @JsonProperty("batchNo")
    private String batchNo;
    @ApiModelProperty("客户列表")
    @JsonProperty("list")
    private List<CustomerInformationDTO> list;
}
