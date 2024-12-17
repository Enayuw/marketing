package com.br.marketing.enums;

public enum ScoreStatusEnum {

    RUNNING(3,"跑分中")
    ,MERGE(1,"待合并")
    ,PUSH(0,"待推送")
    ,FINISH(2,"结束")
    ,OFFLINEMERGE(4,"离线待合并")
    ,OFFLINESFP(5,"已合并待推ftp")
    ,OFFLINECALLBACK(6,"离线待回调")
    ,OFFLINESUCCESS(7,"离线跑批成功")
    ,OFFLINEFAIL(9,"离线跑批失败")
    ,PAUSE(10,"暂停中")
    ,PAUSEED(11,"已暂停")
    ,WAIT_RETRY(12,"异常待重试");

    ScoreStatusEnum(Integer value,String desc) {
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
