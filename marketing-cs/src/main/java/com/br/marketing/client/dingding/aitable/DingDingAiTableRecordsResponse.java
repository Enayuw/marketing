package com.br.marketing.client.dingding.aitable;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 钉钉AI表格记录响应
 * 
 * @author hong.chen
 * @date 2025-10-29
 */
@Data
public class DingDingAiTableRecordsResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 记录列表
     */
    private List<DingDingAiTableRecordDTO> records;
    
    /**
     * 下一页标记
     */
    private String nextToken;
    
    /**
     * 是否还有更多数据
     */
    private Boolean hasMore;
}

