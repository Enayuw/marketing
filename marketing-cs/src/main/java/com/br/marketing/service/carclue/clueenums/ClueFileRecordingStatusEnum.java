package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * @ClassName ClueFileRecordingStatusEnum
 * @Description 车线索文件上传记录状态
 * @Author kongbx
 * @Date 2025/5/6 10:13
 */
@Getter
public enum ClueFileRecordingStatusEnum {

    AWAIT_CLEAN(0, "待清洗"),
    CLEAN_ING(1, "清洗中"),
    CLEAN_FINISH(2, "清洗完成");


    ClueFileRecordingStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;

}
