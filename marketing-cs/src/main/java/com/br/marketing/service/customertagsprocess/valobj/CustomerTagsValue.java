package com.br.marketing.service.customertagsprocess.valobj;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

public class CustomerTagsValue {

    // /**
    //  * 上传数据解析 校验3K的枚举值
    //  */
    // @AllArgsConstructor
    // @Getter
    // public  enum CheckTypeEnum implements ValueInterace {

    //     CHECKCELL(1,"校验cell","checkCellServiceImpl"),
    //     NOCHECK3K(2,"不校验3K","noCheckServiceImpl");

    //     private Integer value;
    //     private String desc;
    //     private String bean;

    // }

    @AllArgsConstructor
    @Getter
    public  enum PushJc3keyTypeEnum implements ValueInterace {

        INIT(0,"软交换","noCheckServiceImpl"),
        MD5_ALL(1,"3Kmd5","checkCellServiceImpl"),
        SHA256_ALL(2,"3Ksha256","checkCellServiceImpl"),
        PLAINTEXT(3,"log加密","checkCellServiceImpl"),
        AES_COMMON(4,"AES通用","aesCommonStrategy"),
        AES_NMD(5,"AES你我贷定制版","aesNmdStrategy"),
        SM3(6,"SM3国密哈希","sm3CheckServiceImpl"),
        SM4(7,"SM4国密加密","sm4CheckServiceImpl");

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

    /**
     * 自动将枚举转换为JSON（AES通用=0, AES定制=1, 其他=2）
     */
    public static String convertPushJc3keyEnumToJson() {
        Map<String, JSONObject> resultMap = new LinkedHashMap<>();

        for (PushJc3keyTypeEnum enumItem : PushJc3keyTypeEnum.values()) {
            JSONObject innerJson = new JSONObject();
            innerJson.put("desc", enumItem.getDesc());

            // 自动判断状态
            int status = 2; // 默认其他=2
            if (enumItem == PushJc3keyTypeEnum.AES_COMMON
                    || enumItem == PushJc3keyTypeEnum.SM4) {
                status = 0;
            } else if (enumItem == PushJc3keyTypeEnum.AES_NMD) {
                status = 1;
            }

            innerJson.put("status", status);
            resultMap.put(String.valueOf(enumItem.getValue()), innerJson);
        }

        return JSONObject.toJSONString(resultMap, true);
    }

}
