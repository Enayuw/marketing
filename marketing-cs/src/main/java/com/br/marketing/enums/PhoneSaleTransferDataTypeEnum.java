package com.br.marketing.enums;

/**
 * 电销转化数据类型
 * <p>
 * 数据类型(其他业务功能依次增加编号，并用枚举维护) 1:转化；2:失效数据过滤
 *
 * @author Guo Zeqiang
 * @dateTime 2022/7/15 10:41
 */
public enum PhoneSaleTransferDataTypeEnum {
    // 转化
    TRANSFER(1),
    // 失效数据过滤
    INVALID_DATA_FILTER(2);
    private int value;

    PhoneSaleTransferDataTypeEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
