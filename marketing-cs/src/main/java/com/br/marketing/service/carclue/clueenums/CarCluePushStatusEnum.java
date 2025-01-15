package com.br.marketing.service.carclue.clueenums;

import lombok.Getter;

/**
 * 车线索推送状态枚举
 *
 * @author zhen.li1
 * @date 2025/01/15
 */
@Getter
public enum CarCluePushStatusEnum {

    READY(0, "待推送"),
    SUCCESS(1, "推送成功"),
    FAIL(2, "推送失败");


    CarCluePushStatusEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;





}
