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

        INIT(0,"软交换","noCheckServiceImpl"),
        MD5_ALL(1,"3Kmd5","checkCellServiceImpl"),
        SHA256_ALL(2,"3Ksha256","checkCellServiceImpl"),
        PLAINTEXT(3,"log加密","checkCellServiceImpl"),
        AES_COMMON(4,"AES通用","aesCommonStrategy"),
        AES_NMD(5,"AES你我贷定制版","aesNmdStrategy");

        private Integer value;
        private String desc;
        private String strategyBean;
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
        return null;
    }

    /**
     * 定义通用接口
     */
    public interface ValueInterace {
        Integer getValue();
    }
}
