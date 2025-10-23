package com.br.marketing.service.mock.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName MockPolicyEnum
 * @Description mock策略类型
 * @Author kongbx
 * @Date 2025/6/30 17:28
 */
@Getter
public enum MockPolicyEnum {
    FIXED(0, "固定"),
    RANDOM(1, "随机");

    MockPolicyEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private Integer code;
    private String desc;

    public static Map<Integer, String> getAllMockPolicyEnum() {
        return Arrays.stream(MockPolicyEnum.values())
                .collect(Collectors.toMap(
                        MockPolicyEnum::getCode,
                        MockPolicyEnum::getDesc,
                        (oldValue, newValue) -> oldValue
                ));
    }

}
