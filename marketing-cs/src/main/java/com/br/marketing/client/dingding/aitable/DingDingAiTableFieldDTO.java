package com.br.marketing.client.dingding.aitable;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import java.io.Serializable;

/**
 * 钉钉AI表格字段DTO
 * 
 * @author hong.chen
 * @date 2025-10-29
 */
@Data
public class DingDingAiTableFieldDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 字段ID
     */
    private String id;
    
    /**
     * 字段名称
     */
    private String name;
    
    /**
     * 字段类型
     * 例如：text, singleSelect, number 等
     */
    private String type;
    
    /**
     * 字段属性（如选项列表、formatter等）
     */
    private JSONObject property;
    
    /**
     * 获取字段的formatter
     */
    public String getFormatter() {
        if (property != null) {
            return property.getString("formatter");
        }
        return null;
    }
}

