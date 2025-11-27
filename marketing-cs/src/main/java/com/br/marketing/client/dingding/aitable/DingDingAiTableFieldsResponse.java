package com.br.marketing.client.dingding.aitable;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 钉钉AI表格字段响应
 * 
 * @author hong.chen
 * @date 2025-10-29
 */
@Data
public class DingDingAiTableFieldsResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 字段列表
     */
    private List<DingDingAiTableFieldDTO> value;
}

