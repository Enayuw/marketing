package com.br.marketing.enums;

/**
 * colliding_data_deal_status枚举
 */
public enum XyfSyncStatusEnum {

    SYNC_WAIT(0,"未上传"),
    SYNCING(1,"上传中"),
    SYNC_SUCCESS(2,"上传成功"),
    SYNC_FAIL(3,"上传失败");

    XyfSyncStatusEnum(int code, String desc){
        this.code = code;
        this.desc = desc;
    }
    private int code;

    private String desc;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
