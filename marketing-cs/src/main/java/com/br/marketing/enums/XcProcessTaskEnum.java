package com.br.marketing.enums;

import lombok.Getter;

@Getter
public enum XcProcessTaskEnum {

    PROCESS_FALSE(0 ),
    PROCESS_DELETE(1),
    PROCESS_DYNA_FALSE(3),
    PROCESS_BALCKLIST_DELETE(4);

    private Integer taskType;

    XcProcessTaskEnum(Integer taskType) {
        this.taskType = taskType;
    }


}
