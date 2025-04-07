package com.br.marketing.enums;

/**
 * @ClassName EsSyncStatusEnum
 * @Description es同步状态
 * @Author kongbx
 * @Date 2025/2/19 16:33
 */
public enum EsSyncStatusEnum {

    SYNCING(0,"同步中")
    ,COMPLETE(1,"同步完成")
    ,INITIAL(null,"初始状态");

    EsSyncStatusEnum(Integer value,String desc) {
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
