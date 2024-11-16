package com.br.marketing.service.customertagsprocess.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class CustomerTagsValue {

    /**
     * 上传数据解析 校验3K的枚举值
     */
    @AllArgsConstructor
    @Getter
    public  enum CheckTypeEnum implements ValueInterace {

        CHECKCELL(1,"校验cell","checkCellServiceImpl"),
        NOCHECK3K(2,"不校验3K","noCheckServiceImpl");

        private Integer value;
        private String desc;
        private String bean;

    }

    @AllArgsConstructor
    @Getter
    public  enum PushJc3keyTypeEnum implements ValueInterace {

        INIT(0,"原文"),
        MD5_ALL(1,"3Kmd5"),
        SHA256_ALL(2,"3Ksha256");

        private Integer value;
        private String desc;

    }

    /**
     * 根据指定枚举获取
     * @param value
     * @param enumClass
     * @return
     * @param <E>
     */
    public static <E extends Enum<E> & ValueInterace> E getEnumByValue(Integer value, Class<E> enumClass) {
        for (E enumConstant : enumClass.getEnumConstants()) {
            if (((ValueInterace) enumConstant).getValue().equals(value)) {
                return enumConstant;
            }
        }
        return null; // 或抛出异常
    }

    /**
     * 定义通用接口
     */
    public interface ValueInterace {
        Integer getValue();
    }
}
