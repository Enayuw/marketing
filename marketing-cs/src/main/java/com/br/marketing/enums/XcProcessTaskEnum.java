package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XcProcessTaskEnum {

    PROCESS_FALSE(0, null, null),
    PROCESS_DELETE(1, 0, "xcCollidingDelete"),
    PROCESS_POLICY(2, null, null),
    PROCESS_DYNA_FALSE(3, 1, "xcCollidingDeleteForDyna"),
    PROCESS_BALCKLIST_DELETE(4, 2, "xcCollidingDeleteForBlack");

    private Integer taskType;

    private Integer batchType;

    private String deleteRedisKey;

    XcProcessTaskEnum(Integer taskType, Integer batchType, String deleteRedisKey) {
        this.taskType = taskType;
        this.batchType = batchType;
        this.deleteRedisKey = deleteRedisKey;
    }


}
