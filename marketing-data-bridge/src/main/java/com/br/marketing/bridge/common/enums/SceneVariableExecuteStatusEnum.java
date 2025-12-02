package com.br.marketing.bridge.common.enums;

/**
 * @ClassName SceneVariableExecuteStatusEnum
 * @Description 场景变量执行状态枚举
 * @Author kongbx
 * @Date 2025/12/02
 */
public enum SceneVariableExecuteStatusEnum {

    /**
     * 待执行
     */
    PENDING(0, "待执行"),

    /**
     * 执行完成
     */
    COMPLETED(1, "执行完成"),

    /**
     * 执行失败
     */
    FAILED(2, "执行失败"),

    /**
     * 未找到上传数据
     */
    NOT_FOUND_UPLOAD(3, "未找到上传数据");

    private final Integer code;
    private final String desc;

    SceneVariableExecuteStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code获取枚举
     */
    public static SceneVariableExecuteStatusEnum getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (SceneVariableExecuteStatusEnum statusEnum : values()) {
            if (statusEnum.getCode().equals(code)) {
                return statusEnum;
            }
        }
        return null;
    }
}

