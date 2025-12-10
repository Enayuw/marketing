package com.br.marketing.enums;

/**
 * package_type枚举
 */
public enum TcCpaCollidingTaskPackageStatus {

    STATUS_WAIT_EXECUTE(0,"待执行"),
    STATUS_EXECUTING(1,"执行中"),
    STATUS_EXECUTED(2,"执行完成"),
    ;
    TcCpaCollidingTaskPackageStatus(Integer value, String desc){
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
