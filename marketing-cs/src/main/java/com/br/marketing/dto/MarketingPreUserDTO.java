package com.br.marketing.dto;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@Data
public class MarketingPreUserDTO  implements Serializable {

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
