package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * @ClassName ExecuteClueStatusEnum
 * @Author kongbx
 * @Date 2025/5/6 15:17
 */
@Getter
public enum ExecuteClueStatusEnum {
    AWAIT_EXECUTE(0, "待执行"),
    EXECUTE_FINISH(1, "执行完成"),
    EXECUTE_ERROR(2, "执行失败");


    ExecuteClueStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;
}
