package com.br.marketing.client.dingding.aitable;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import java.io.Serializable;

/**
 * 钉钉AI表格记录DTO
 * 
 * @author hong.chen
 * @date 2025-10-29
 */
@Data
public class DingDingAiTableRecordDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 记录ID
     */
    private String id;
    
    /**
     * 字段数据
     */
    private JSONObject fields;
    
    /**
     * 创建人信息
     */
    private JSONObject createdBy;
    
    /**
     * 创建时间
     */
    private Long createdTime;
    
    /**
     * 最后修改人信息
     */
    private JSONObject lastModifiedBy;
    
    /**
     * 最后修改时间
     */
    private Long lastModifiedTime;
}

