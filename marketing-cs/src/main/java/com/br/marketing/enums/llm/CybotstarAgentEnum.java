package com.br.marketing.enums.llm;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Cybotstar 智能体编码枚举
 */
@Getter
@AllArgsConstructor
public enum CybotstarAgentEnum {

    /**
     * Aviator 脚本生成器
     */
    SCRIPT_GENERATOR("Aviator_SCRIPT_GENERATOR", "Aviator脚本生成器"),

    ;

    /**
     * 智能体编码
     */
    private final String code;

    /**
     * 智能体名称
     */
    private final String name;

    /**
     * 根据 code 获取枚举
     */
    public static CybotstarAgentEnum getByCode(String code) {
        for (CybotstarAgentEnum agent : values()) {
            if (agent.getCode().equals(code)) {
                return agent;
            }
        }
        return null;
    }

}
