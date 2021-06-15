package com.br.marketing.dto;

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
     * 客户数据
     */
    private List<MarketingPreUserDetailDTO> dataItems;
}
