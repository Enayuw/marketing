package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XcProcessTaskEnum {

    /**
     * @description false包补充
     * @date 2024/11/12 20:44
     **/
    PROCESS_FALSE(0, null, null),
    /**
     * @description true包剔除
     * @date 2024/11/12 20:44
     **/
    PROCESS_DELETE(1, 1, "xcCollidingDelete"),
    /**
     * @description 推送决策
     * @date 2024/11/12 20:44
     **/
    PROCESS_POLICY(2, null, null),
    /**
     * @description false动态包剔除
     * @date 2024/11/12 20:44
     **/
    PROCESS_DYNA_FALSE(3, 3, "xcCollidingDeleteForDyna"),
    /**
     * @description 黑名单剔除
     * @date 2024/11/12 20:44
     **/
    PROCESS_BALCKLIST_DELETE(4, null, "xcCollidingDeleteForBlack");

    private Integer taskType;

    private Integer batchType;

    private String deleteRedisKey;

    XcProcessTaskEnum(Integer taskType, Integer batchType, String deleteRedisKey) {
        this.taskType = taskType;
        this.batchType = batchType;
        this.deleteRedisKey = deleteRedisKey;
    }


}
