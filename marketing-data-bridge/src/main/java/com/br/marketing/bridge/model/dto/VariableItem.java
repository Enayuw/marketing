package com.br.marketing.bridge.model.dto;

/**
 * @ClassName VariableItem
 * @Description 场景变量项
 * @Author kongbx
 * @Date 2025/12/02
 */
public class VariableItem {
    
    /**
     * 变量编码
     */
    private String code;
    
    /**
     * 变量值
     */
    private String value;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "VariableItem{" +
                "code='" + code + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

