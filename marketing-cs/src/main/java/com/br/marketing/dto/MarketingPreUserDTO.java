package com.br.marketing.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MarketingPreUserDTO implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * 任务id
     */
    private String taskId;

    /**
     * 请求批次号
     */
    private String requestId;

    /**
     * 是否最后一次，0:非最后一次，1:最后一次
     * */
    @Schema(description = "是否最后一次，0:非最后一次，1:最后一次")
    private String last;

    @Schema(description = "总数据量")
    /**
     * 总数据量
     * */
    private String total;

    /**
     * 客户数据
     */
    private List<MarketingPreUserDetailDTO> dataItems;

    /**
     * 0:通用调用 1:定制接口清洗后调用
     * 默认为0
     */
    private Integer dataSourceType;
}
