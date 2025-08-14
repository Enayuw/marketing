package com.br.marketing.enums;

/**
 * is_del枚举
 */
public enum TcCpaIsDelEnum {

    DEL_NO(1,"可用"),
    DEL_YES(9,"删除");
    TcCpaIsDelEnum(Integer value, String desc){
          this.value = value;
            this.desc=desc;
    }
    private Integer value;

    private String desc;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
