package com.br.marketing.enums;

/**
 * 文件类型枚举
 */
public enum FileTypeEnum {

    YYYP_DATA(1, "营销数据文件"),
    RESULT_DATA(2, "结果数据文件"),
    DAE_DATA(3, "DAE数据文件"),
    HIST_DATA(4, "历史数据文件"),
    PASS_DATA(5, "通过数据文件"),
    YIXIN_REAL_DATA(6, "宜信实时数据文件"),
    YIXIN_DENY_DATA(7, "宜信拒绝数据文件"),
    YIXIN_TRANSFER_DATA(8, "宜信转化数据文件"),
    CAR_DATA(99, "车线索数据文件"),
    SHORK_LINK_DATA(100, "短链数据文件");

    FileTypeEnum(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    private Integer value;

    private String desc;

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * 根据值获取枚举
     */
    public static FileTypeEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (FileTypeEnum type : values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 判断值是否有效
     */
    public static boolean isValid(Integer value) {
        return getByValue(value) != null;
    }

    /**
     * 获取描述
     */
    public static String getDescByValue(Integer value) {
        FileTypeEnum type = getByValue(value);
        return type != null ? type.getDesc() : null;
    }
} 