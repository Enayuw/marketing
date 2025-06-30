package com.br.marketing.service.mock.enums;

import lombok.Getter;

/**
 * @ClassName MockNameEnum
 * @Description mock名称枚举
 * @Author kongbx
 * @Date 2025/6/30 10:30
 */
@Getter
public enum MockNameEnum {

    TEST_POLLING(0, "测试轮询"),
    TEST_RANDOM(1, "测试随机");


    MockNameEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;


}
