package com.br.marketing.enums;

/**
 * 任务状态枚举值
 */
public enum JobStatusEnum {

    START(1),FINISH(2),RETRY(3),RETRY_FAIL(4);

    JobStatusEnum(Integer value){
        this.value = value;
    }

    private Integer value;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
