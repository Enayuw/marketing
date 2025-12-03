package com.br.marketing.enums;

/**
 * @ClassName TaskTypeEnum
 * @Description 任务类型 0：跑分任务，1：上传任务
 * @Author kongbx
 * @Date 2025/11/1 15:37
 */
public enum TaskTypeEnum {
    SCORE_TASK(0,"跑分任务"),
    UPLOAD_TASKS(1,"上传任务");

    TaskTypeEnum(Integer value, String desc) {
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
