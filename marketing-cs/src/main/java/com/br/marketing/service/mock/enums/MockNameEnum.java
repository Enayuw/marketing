package com.br.marketing.service.mock.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName MockNameEnum
 * @Description mock名称枚举
 * @Author kongbx
 * @Date 2025/6/30 10:30
 */
@Getter
public enum MockNameEnum {
    TEST_POLLING("1001", "测试轮询"),
    TEST_RANDOM("1002", "测试随机");

    MockNameEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private String code;
    private String desc;

    // 获取所有code的列表
    public static List<String> getAllCodes() {
        return Arrays.stream(values())
                .map(MockNameEnum::getCode)
                .collect(Collectors.toList());
    }
}
